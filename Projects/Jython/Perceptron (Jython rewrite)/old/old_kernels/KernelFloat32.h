/* Kernel.java
 *
 * The plan is to re-implement perceptron with most of the fuzzy stuff in
 * Jython. I'm not sure about this plan. Certainly the main rendering must
 * be handled in Java proper.
 * 
 * This includes: 
 * -- map lookups
 * -- image lookups
 * -- compositing
 * 
 * The place in my heart for psychedelic rock will never harden.
 * 
 * Vector graphics may be handeled by Jython. User interface configuration as
 * well. The goal is to reduce the size and complexity of the code.
 * 
 * ----------------------------------------------------------------------------
 * 
 * 
 */

#define LENGTH (WIDTH*HEIGHT)
#define H_1 (HEIGHT-1)
#define W_1 (WIDTH-1)
#define H_3 (HEIGHT-3)
#define W_3 (WIDTH-3)
#define W8 (W_3*256)
#define H8 (H_3*256)
#define W_82 (W8*2)
#define H_82 (H8*2)

#define FIXED 0x100
#define FLIRP(a,x1,x2)  (a*(x1)+(FIXED-a)*(x2))

// Java likes the int ARGB format. We can do some color operations on this
// format efficiently, but adding and combining the colors requires some
// separation of channels to avoid overflow issues. Linear interpolation can
// still do some tricks but is more verbose.
#define R32M  0xff0000
#define G32M  0x00ff00
#define B32M  0x0000ff
#define V32M  0xff00ff
#define D32M  0x800080
#define S32M  0x008000
#define W32M  0xffffff
#define I32M  0x010101
#define O32M  0xfefefe

#define B32(c)   (c    &0xff)
#define G32(c)   (c>> 8&0xff)
#define R32(c)   (c>>16&0xff)

#define PACKRGB32(r,g,b) (int)((r)<<16|(g)<<8|(b))
#define CLIRP32(a,x,y) FLIRP(a,x&V32M,y&V32M)+D32M>>8&V32M|FLIRP(a,x&G32M,y&G32M)+S32M>>8&G32M

#define CLIPCOLOR(v) ((v)>0xff?0xff:(v)<0?0:(v))
#define CLIPROUNDED(v) v>0xffff?0xff:v<0?0:v>>8

import java.awt.Color;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.concurrent.atomic.AtomicInteger;

public class KERNELVERSION 
{
	public static class ColorFilterParameters
	{
		public int HUE;
		public int SATURATION;
		public int LIGHTEN;
		public int DARKEN;
		public int BRIGHTNESS;
		public int NONLINEARCONTRAST;
		public int CONTRAST;
		public int WEIGHT;
		public int INVERT;
		public int ENABLE;
	}
	
	static final public ColorFilterParameters Post        = new ColorFilterParameters();
	static final public ColorFilterParameters Recurrent   = new ColorFilterParameters();
	static final public ColorFilterParameters Homeostatic = new ColorFilterParameters();

	static final Random rng = new Random( 19580427 );
	
	// parameters
	public static int HTAU;
	public static int MOTIONBLUR;
	public static int BLUR;
	public static int SHARPEN;
	public static int NOISE;
	public static int T1;
	public static int T2;
	public static int T3;
	public static int T4;
	public static int T5;
	public static int T6;
	public static int[] mapping;
	
	public static int INTERPOLATION=0;
	public static int BOUNDARY=0;
	public static int TREE=0;
	public static int HSV_OPERATOR=1;
	
	// configuration variables and internal data buffers
	static int [] din,dtmp,dout;
	public static BufferedImage out;
	public static BufferedImage disp;

	// secret stuff
	static int gRMEAN,gGMEAN,gBMEAN,gRVAR,gGVAR,gBVAR,gRMIN,gRMAX,gBMIN,gBMAX,gGMIN,gGMAX,RSCALE,GSCALE,BSCALE;
	static int RMEAN=0,GMEAN=0,BMEAN=0;
	static int RVAR=0,GVAR=0,BVAR=0;
	static int RMIN=0xff, RMAX=0, GMIN=0xff, GMAX=0, BMIN=0xff, BMAX=0;
	
	public static void prepare()//(int w, int h)
	{	
		out  = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		disp = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		din  = new int[LENGTH];
		dtmp = new int[LENGTH];
		dout = new int[LENGTH];
	}

	public static void convolveStage1(int threadnumber, int nthreads)
	{
		if (0==BLUR&&0==SHARPEN) return;
		
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		int curr, next, prev, pair;
		
		int Cstart = WIDTH/nthreads*threadnumber;
		int Cstop  = threadnumber==nthreads-1? WIDTH: WIDTH/nthreads*(threadnumber+1);
		
		for (int C=Cstart; C<Cstop; C++) 
		{
			next = bufferOut.getElem(C+WIDTH) & O32M;
			curr = bufferOut.getElem(C  ) & O32M;
			bufferTmp[C] = pair = prev = next+curr>>1 & O32M;
			for (int R=1; R<H_1; R++) 
			{
				curr = next; next = bufferOut.getElem(C+(R+1)*WIDTH) & O32M;
				prev = pair; pair = next+curr>>1 & O32M;
				bufferTmp[C+R*WIDTH]=prev+pair>>1;
			}
			bufferTmp[C+H_1*WIDTH]=pair>>1;
		}
	}
		
	public static void convolveStage2(int threadnumber, int nthreads)
	{
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		
		int Rstart = HEIGHT/nthreads*threadnumber;
		int Rstop  = threadnumber==nthreads-1? HEIGHT: HEIGHT/nthreads*(threadnumber+1);
		int Istart = Rstart*WIDTH;
		int Istop  = Rstop*WIDTH;
		
		if (0!=BLUR||0!=SHARPEN)
		{
			int curr, next, prev, pair;
			for (int R=Rstart; R<Rstop; R++) 
			{
				next = bufferTmp[R*WIDTH+1] & O32M;
				curr = bufferTmp[R*WIDTH  ] & O32M;
				bufferIn[R*WIDTH] = pair = prev = next+curr>>1 & O32M;
				for (int C=1; C<W_1; C++) 
				{
					curr = next; next = bufferTmp[R*WIDTH+C+1] & O32M;
					prev = pair; pair = next+curr>>1 & O32M;
					bufferIn[R*WIDTH+C]= prev+pair>>1;
				}
				bufferIn[R*WIDTH+W_1]=pair;
			}
			
			if (BLUR!=0) for (int i=Istart;i<Istop;i++)
			{
				int c1 = bufferIn[i];
				int c2 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(BLUR,c1,c2);
			}
			if (SHARPEN!=0) for (int i=Istart;i<Istop;i++)
			{
				int c2 = ~bufferIn[i]&W32M;
				int c1 = bufferOut.getElem(i);
				bufferIn[i] = CLIRP32(SHARPEN,c2,c1);
			}
		}
		else for (int i=Istart;i<Istop;i++) bufferIn[i] = bufferOut.getElem(i);		
	}
		
	public static void stat()
	{ 
		int[] bufferIn  = din;
		int[] bufferTmp = dtmp;
		DataBuffer bufferOut = out.getRaster().getDataBuffer();
		
		RMEAN=GMEAN=BMEAN=0;
		RVAR=GVAR=BVAR=0;
		RMIN=GMIN=BMIN=0xff; 
		RMAX=GMAX=BMAX=0;
		for (int i=0;i<LENGTH;i++)
		{
			int c = bufferIn[i];
			int r = R32(c);
			int g = G32(c);
			int b = B32(c);
			RMEAN += r;
			GMEAN += g;
			BMEAN += b;
			if (r<RMIN) RMIN=r;
			if (g<GMIN) GMIN=g;
			if (b<BMIN) BMIN=b;
			if (r>RMAX) RMAX=r;
			if (g>GMAX) GMAX=g;
			if (b>BMAX) BMAX=b;
		}
		RMEAN = (RMEAN+LENGTH/2)/LENGTH;
		GMEAN = (GMEAN+LENGTH/2)/LENGTH;
		BMEAN = (BMEAN+LENGTH/2)/LENGTH;
		for (int i=0;i<LENGTH;i++)
		{
			int c = bufferIn[i];
			int r = R32(c)-RMEAN;
			int g = G32(c)-GMEAN;
			int b = B32(c)-BMEAN;
			RVAR += r*r+0x10>>5;
			GVAR += g*g+0x10>>5;
			BVAR += b*b+0x10>>5;
		}
		RVAR = (RVAR+LENGTH/2)/LENGTH+4>>3;
		GVAR = (GVAR+LENGTH/2)/LENGTH+4>>3;
		BVAR = (BVAR+LENGTH/2)/LENGTH+4>>3;
		RVAR = (int)(sqrt(RVAR)*16+0.5);
		GVAR = (int)(sqrt(GVAR)*16+0.5);
		BVAR = (int)(sqrt(BVAR)*16+0.5);
		RVAR = Homeostatic.CONTRAST*256/(RVAR+2);
		GVAR = Homeostatic.CONTRAST*256/(GVAR+2);
		BVAR = Homeostatic.CONTRAST*256/(BVAR+2);
	}
	
	public static void render(int threadnumber, int nthreads)
	{
		int [] bufferIn = din;
		DataBuffer bufferOut  = out .getRaster().getDataBuffer();
		DataBuffer bufferDisp = disp.getRaster().getDataBuffer();
		
		int  random = rng.nextInt();
		float[] HSV = {0f,0f,0f};
		int Rstart = HEIGHT/nthreads*threadnumber;
		int Rstop  = threadnumber==nthreads-1? HEIGHT: HEIGHT/nthreads*(threadnumber+1);
		int istart = Rstart*WIDTH;
		int istop  = Rstop*WIDTH;
		
		float rBrightness = (Homeostatic.BRIGHTNESS*256-RMEAN*RVAR) / 256.f;
		float gBrightness = (Homeostatic.BRIGHTNESS*256-GMEAN*GVAR) / 256.f;
		float bBrightness = (Homeostatic.BRIGHTNESS*256-BMEAN*BVAR) / 256.f;
		float rContrast   = RVAR/256.f;
		float gContrast   = GVAR/256.f;
		float bContrast   = BVAR/256.f;
		
		float fhuerotate = Recurrent.HUE*6/256.f;
		for (int i=istart; i<istop; i++)
		{
			int x1 = mapping[2*i];
			int y1 = mapping[2*i+1];
			int x = T1*x1+T2*y1+T3 + 0x200 >> 10;
			int y = T4*x1+T5*y1+T6 + 0x200 >> 10;		
			x += (INTERPOLATION-1)*0x80;
			y += (INTERPOLATION-1)*0x80;
			if (INTERPOLATION==2)
			{
				x += 0x80;
				y += 0x80;
			}
			
			switch (BOUNDARY)
			{
				case 0://mirror
				    while (x<W_82) x+=W_82;
				    while (y<H_82) y+=H_82;
				    x  = (x/W8&1)==1? x%W8 : (W8-1)-x%W8;
				    y  = (y/H8&1)==1? y%H8 : (H8-1)-y%H8;
				    break;
				case 1://torus
				    while (x<W8) x+=W8;
				    while (y<H8) y+=H8;
					x%=W8;
					y%=H8;
					break;
				case 2://extend
					x = x<0?0:x>W8?W8:x;
					y = y<0?0:y>H8?H8:y;
					break;
				case 3://circle
				case 4://square
            }
            
			float ax1 = (x&0xff)/256.f;
			float ay1 = (y&0xff)/256.f;
			x1 = x>>8;
			y1 = y>>8;
			y1 *= WIDTH;
			
			float r=0.f,g=0.f,b=0.f;
			switch (INTERPOLATION) {
				case 0: {// nearest
					int c1 = bufferIn[x1+y1];
					r = R32(c1);
					g = G32(c1);
					b = B32(c1);
				break;}
				case 1: {// linear
					int x2 = x1+1;
					int y2 = y1+1*WIDTH;
					int c1,c2,c3,c4;
					float bx1 = 1.0f-ax1;
					float by1 = 1.0f-ay1;
					c1 = bufferIn[x1+y1];
					c2 = bufferIn[x2+y1];
					float d1r = ax1*R32(c2)+bx1*R32(c1);
					float d1g = ax1*G32(c2)+bx1*G32(c1);
					float d1b = ax1*B32(c2)+bx1*B32(c1);
					c1 = bufferIn[x1+y2];
					c2 = bufferIn[x2+y2];
					float d2r = ax1*R32(c2)+bx1*R32(c1);
					float d2g = ax1*G32(c2)+bx1*G32(c1);
					float d2b = ax1*B32(c2)+bx1*B32(c1);
					r = ay1*d2r+by1*d1r;
					g = ay1*d2g+by1*d1g;
					b = ay1*d2b+by1*d1b;
				break;}
				case 2: {// cubic
					float ax2 = ax1*ax1;
					float ay2 = ay1*ay1;
					float ax3 = ax1*ax2;
					float ay3 = ay1*ay2;
					float bx1 = ax2-0.5f*(ax1+ax3);
					float bx2 = 1.0f-ax2*2.5f+ax3*1.5f;
					float bx3 = 0.5f*ax1+ax2*2.0f-ax3*1.5f;
					float bx4 = 0.5f*(ax3-ax2);
					float by1 = ay2-0.5f*(ay1+ay3);
					float by2 = 1.0f-ay2*2.5f+ay3*1.5f;
					float by3 = 0.5f*ay1+ay2*2.0f-ay3*1.5f;
					float by4 = 0.5f*(ay3-ay2);
					int x2 = x1+1;
					int x3 = x1+2;
					int x4 = x1+3;
					int y2 = y1+1*WIDTH;
					int y3 = y1+2*WIDTH;
					int y4 = y1+3*WIDTH;
					int c1,c2,c3,c4;
					c1 = bufferIn[x1+y1];
					c2 = bufferIn[x2+y1];
					c3 = bufferIn[x3+y1];
					c4 = bufferIn[x4+y1];
					float d1r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
					float d1g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
					float d1b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
					c1 = bufferIn[x1+y2];
					c2 = bufferIn[x2+y2];
					c3 = bufferIn[x3+y2];
					c4 = bufferIn[x4+y2];
					float d2r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
					float d2g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
					float d2b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
					c1 = bufferIn[x1+y3];
					c2 = bufferIn[x2+y3];
					c3 = bufferIn[x3+y3];
					c4 = bufferIn[x4+y3];
					float d3r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
					float d3g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
					float d3b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
					c1 = bufferIn[x1+y4];
					c2 = bufferIn[x2+y4];
					c3 = bufferIn[x3+y4];
					c4 = bufferIn[x4+y4];
					float d4r = bx1*R32(c1)+bx2*R32(c2)+bx3*R32(c3)+bx4*R32(c4);
					float d4g = bx1*G32(c1)+bx2*G32(c2)+bx3*G32(c3)+bx4*G32(c4);
					float d4b = bx1*B32(c1)+bx2*B32(c2)+bx3*B32(c3)+bx4*B32(c4);
					r = by1*d1r+by2*d2r+by3*d3r+by4*d4r;
					g = by1*d1g+by2*d2g+by3*d3g+by4*d4g;
					b = by1*d1b+by2*d2b+by3*d3b+by4*d4b;
				break;}
			}
			
			r = r*rContrast+rBrightness;
			g = g*gContrast+gBrightness;
			b = b*bContrast+bBrightness;
			int c;
			if (HSV_OPERATOR==1)
			{
				/*
				Color.RGBtoHSB(R,G,B,HSV);
				float s = HSV[1]*Recurrent.SATURATION*0.0078125f;
				s = s<0f?0f:s>1f?1f:s;
				c = Color.HSBtoRGB(HSV[0]+Recurrent.HUE*0.00390625f,s,HSV[2]);
				*/
				float min    = r<b?g<r?g:r:g<b?g:b; 
				float value  = r>b?g>r?g:r:g>b?g:b; 
				float C      = value - min;
				if (c!=0.f) //with no saturation, hue is undefined
				{
					float hue;
					if      (value==r) hue = 6+(g-b)/C;
					else if (value==g) hue = 2+(b-r)/C;
					else               hue = 4+(r-g)/C;
					hue += fhuerotate;
					hue %= 6.f;
					r=g=b=min;
					switch((int)hue)
					{
						case 0: r+=C; g+=C*hue; break;
						case 1: r+=C*(2-hue); g+=C; break;
						case 2: g+=C; b+=C*(hue-2); break;
						case 3: g+=C*(4-hue); b+=C; break;
						case 4: r+=C*(hue-4); b+=C; break;
						case 5: r+=C; b+=C*(6-hue); break;
					}
				}
			}
			int R = r<0.f?0:r>=256.f?0xff:(int)r;
			int	G = g<0.f?0:g>=256.f?0xff:(int)g;
			int	B = b<0.f?0:b>=256.f?0xff:(int)b;
			c = PACKRGB32(R,G,B);
			c = CLIRP32(MOTIONBLUR,bufferIn[i],c); //Motion Blur

			random^=random>>19^random<<11^random<<3;
            c = CLIRP32(NOISE,random,c); //Fade with noise

			bufferOut.setElem(i,c);
		}
	}
	
	/*	
	void RgbToHsb()
	{
		max = r>b?r>g?r:g:b>g?g:b;
		min = r<b?r<g?r:g:b<g?g:b;
		delta = max - min;
		brightness = max;
		saturation = max==0?0:delta/max;
		if (delta == 0) hue = 0;
		else {
			if (r==max)      hue = 1*43+(g-b)*43/delta;
			else if (g==max) hue = 2*43+(b-r)*43/delta;
			else             hue = 4*43+(r-g)*43/delta;
			if (hue < 0) hue += 256;
			outHsb->hue = hue;
		}
	}
	*/
}










