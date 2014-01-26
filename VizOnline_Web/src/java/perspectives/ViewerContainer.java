package perspectives;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import perspectives.Task;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.keypoint.PngEncoder;

import properties.Property;


/**
 * This is a window class for Viewers. It will be able to accommodate any of the three main types of Viewers: 2D, 3D, GUI. It will call their rendering, simulation, and interactivity functions (if appropriate), it will implement double buffering for them, and it can be added to the viewer area
 * of the Environment. ViewerContainers automatically implementing panning and zooming for 2D viewers via 2dtransforms applied to the Graphics2D objects passed onto the Viewer2D's render function.
 * @author rdjianu
 *
 */
public class ViewerContainer{
	
	//used to implement double buffering for 2d and 3d viewers
	private BufferedImage viewerImage = null;
	private BufferedImage image = null;
	private BufferedImage lastImage = null;
	
	Object o1 = new Object();
	Object o2 = new Object();
	
	int tilesX = 2;
	int tilesY = 2;
	
	
	ViewerWindow window = null;
		 
	Viewer viewer;	
		
	//a pointer to the parent Environment class (needed for instance to delete the viewer from the Environment if the user activates the 'X')
	Environment env;
	
	int width, height;
	
	long lastMouseMove;
	
	boolean tooltipOn;
	String prevTooltip;
	
	BufferedImage savedImage = null;
	boolean blocked = false;
	
	public ViewerContainer(Viewer v, Environment env, int width, int height)
	{		
		this.env = env;		
		
		this.width = width;
		this.height = height;
		
		tooltipOn = false;
		String prevTooltip = "";
		
		viewer = v;
		viewer.setContainer(this);	
	}

	
	public void setWidth(int w)
	{
		width = w;
	}
	public void setHeight(int h)
	{
		height = h;
	}
	public int getWidth()
	{
		return width;
	}
	public int getHeight()
	{
		return height;
	}
	
	public void render()
	{

	}
	
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public void setViewerImage(BufferedImage im)
	{		
		this.viewerImage = im;
		
		if (!blocked)
			changeImage(im);
		else
			this.savedImage = im;
		
	    if (tooltipOn && prevTooltip.equals(viewer.getToolTipText()))
	     {
	        		BufferedImage imm = new BufferedImage(viewerImage.getWidth(), viewerImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D gc = (Graphics2D)imm.getGraphics();
					
					gc.drawImage(viewerImage, 0, 0, null);					
					viewer.renderTooltip(gc);
					if (!blocked)					
						changeImage(imm);
					else
						savedImage = imm;
	     }
	     else
	     {
	    	 tooltipOn = false;
	    	 prevTooltip = "";
	     }
	}
	

	

	
	public void keyPressed(KeyEvent e)
	{
		
	}

	public void keyReleased(KeyEvent e) {					
	}
	
	public void keyTyped(KeyEvent arg0) {
	}	
	
	
	public void mouseClicked(int ex, int ey, int button) {
	}
	public void mouseEntered(int ex, int ey) {
	}
	public void mouseExited(int ex, int ey) {
	}
	public void mousePressed(int ex, int ey, int button) {

	}
	public void mouseReleased(int ex, int ey, int button)
	{
		
	}		
	
	public void mouseDragged(int ex, int ey) {
		

	}
	public void mouseMoved(int ex, int ey)
	{
		
	}
	
	public boolean unresponsive()
	{
		return viewer.em.unresponsive(2000);
	}
	

	public void block(boolean blocked)
	{
		if (this.blocked == blocked)
			return;
		
		this.blocked = blocked;
		
		Property[] ps = viewer.getProperties();
		for (int i=0; i<ps.length; i++)
			ps[i].setDisabled(blocked);
		
		//lastImage = image;
		
		if (blocked)
		{
			BufferedImage im = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D)im.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.setColor(new Color(100,100,100,100));
			g.fillRect(0,0,im.getWidth(),im.getHeight());
			savedImage = image;
			changeImage(im);
		}
		else if (savedImage != null)
			changeImage(savedImage);
		
		
	}
	
	public void renderTooltip()
	{
		long delta = new Date().getTime()- lastMouseMove;	
		if (delta >= viewer.getTooltipDelay())
		{
			if (viewer.getToolTipText().length() > 0)
			{
				tooltipOn = true;
				prevTooltip = viewer.getToolTipText();
			}
		}
			
	     if (tooltipOn)
	     {
	        		BufferedImage im = new BufferedImage(viewerImage.getWidth(), viewerImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D gc = (Graphics2D)im.getGraphics();
					
					gc.drawImage(viewerImage, 0, 0, null);					
					viewer.renderTooltip(gc);
					
					changeImage(im);
					
	     }  		
	}
	
	public void changeImage(BufferedImage newimage)
	{
		synchronized(o2)
		{
			image = newimage;
		}

	}
	
	Object o3 = new Object();
	byte[][][] tilePngs = null;
	byte[][][] tileDifPngs = null;
	private BufferedImage[][] tiles;
	private BufferedImage[][] tileDifs;
	
	byte[][][] outTiles = null;

	
	byte[] noImage = null;
	
	boolean working = false;
	Object o6 = new Object();
	
		
	int diffcount = 0;
	
	int history = 1;
	
	boolean changeSequenceTest = false;
	BufferedImage lastImageSaved = null;
	
	
	private void tileTasks(BufferedImage image)
	{
		synchronized(o6)
		{
			if (working) return;
			working = true;
		}
		
		//System.out.println("alltask");
		
	
		
		BufferedImage difImage = diffImage(image, lastImage);
		if (diffcount == 0 && history == 0)
		{
			if (changeSequenceTest)
			{
				changeSequenceTest = false;
			}
			else
			synchronized(o6)
			{
				working = false;
				return;
			}
		}
		else if (diffcount > 50000 && history == 0 && !changeSequenceTest)
		{
			synchronized(o6)
			{
				changeSequenceTest = true;

				lastImageSaved = lastImage;
				lastImage = image;
				working = false;
				return;
			}
		}
		else if (diffcount > 50000 && changeSequenceTest)
		{
			changeSequenceTest = false;
			difImage = diffImage(image, lastImageSaved);
			history++;
		}
		else if (diffcount <= 50000 && changeSequenceTest)
		{
			changeSequenceTest = false;
			difImage = diffImage(image, lastImageSaved);
		}
		
		long ttt = new Date().getTime();
		
		
		
		if (diffcount > 50000)
		{
			if (history == 0)
				tiles = this.tileImage(image, tilesX, tilesY, true);
			else
			{
				BufferedImage halfIm = resizeImage(image, 1./(history+1));
				tiles = this.tileImage(halfIm, tilesX, tilesY, false);				
			}
			lastImage = image;
			history++;
			if (history > 4) history = 4;
			System.out.println("gettiles 4");
		}
		else
		{
			System.out.println("gettiles 5");
			if (history == 1)
				tiles = this.tileImage(image, tilesX, tilesY, true);
			else if (history > 1)
			{
				BufferedImage halfIm = resizeImage(image, 1./(history+1));
				tiles = this.tileImage(halfIm, tilesX, tilesY, false);	
			}
			else				
				tiles = this.tileImage(difImage, tilesX, tilesY, false);
			
				
			lastImage = image;
			history--;
			if (history < 0) history = 0;
		}
		
				
		tilePngs = new byte[tiles.length][][];
		for (int i=0; i<tilesX; i++)
		{
			tilePngs[i] = new byte[tiles[i].length][];
			for (int j=0; j<tilesY; j++)
				tilePngs[i][j] = null;
		}
		
		System.out.println("alltask3");
		
		for (int i=0; i<tilesX; i++)
			for (int j=0; j<tilesY; j++)	{
				final int i_f = i;	final int j_f = j;
				Task t = new Task("t"){							
					public void task() {						
						 PngEncoder p = new PngEncoder(tiles[i_f][j_f], true);
				         p.setFilter(PngEncoder.FILTER_NONE);
				             p.setCompressionLevel(2);
				         byte[] bs = p.pngEncode(true);
				         
				         synchronized (o3)
				         {
				        	 tilePngs[i_f][j_f] = bs;					         
				        	 allTasksDone();
				         }
					}
				};				
				t.start();
			}
	}
	
	private void allTasksDone()
	{	
		for (int i=0; i<tilePngs.length; i++)
			for (int j=0; j<tilePngs[i].length; j++)
				if (tilePngs[i][j] == null) return;
		
		synchronized(o6)
		{				
				working = false;
		}
			
		synchronized(o8)
		{
			outTiles = tilePngs;
			o8.notifyAll();
		}
		return;
	}
	
	private void getSendTiles()
	{
		synchronized(o8)
		{
			if (outTiles == null)
			{
				sendTiles = null;
				return;
			}
			
			sendTiles = new byte[outTiles.length][][];
			for (int i=0; i<outTiles.length; i++)
			{
				sendTiles[i] = new byte[outTiles[i].length][];
				for (int j=0; j<outTiles[i].length; j++)
					sendTiles[i][j] = outTiles[i][j];
			}
			
			outTiles = null;
		}
	}
	
	
	
	
	Object o5 = new Object();
	int availableTiles = 0;
	int round = 0;
	Object o7 = new Object();
	Object o8 = new Object();
	
	byte[][][] sendTiles = null;
	
	public void createTiles()
	{
		if (image != lastImage || history > 0)
		{		
			final BufferedImage im = image;
			
			long tm = new Date().getTime();
			Task t = new Task("t")
			{	
				public void task()
				{	
					tileTasks(im);	
				}	
			};				
			t.start();
		}
	}
	
	public byte[] getTile(int x, int y)
	{		
		synchronized(o5)
		{
			if (round == 0)						
				getSendTiles();			
		
			byte[] ret = null;
			if (sendTiles == null || sendTiles[x][y] == null)
				ret = noImage();
			else
			{				
				ret = sendTiles[x][y];
				sendTiles[x][y] = null;	
			}
			
			round++;
			if (round >= tilesX*tilesY) round = 0;
			
			if (round == 0)
				createTiles();
			
			return ret;
		}
	}
	
	public byte[] noImage()
	{
		if (noImage == null)
		{
			BufferedImage im = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D)im.createGraphics();
			g.setColor(new Color(0,0,0,0));
			g.fillRect(0, 0, 1, 1);
			PngEncoder p = new PngEncoder(im, true);
	        p.setFilter(PngEncoder.FILTER_NONE);
	         p.setCompressionLevel(2);
	         noImage = p.pngEncode(true);	
		}
		
		return noImage;
	}

	private BufferedImage diffImage(BufferedImage image, BufferedImage lastImage)
	{
		long ttt = new Date().getTime();
		
			diffcount = 0;
		
	        BufferedImage dif = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
	        dif.createGraphics().drawImage(image, 0,0,null);
	  
	        
	        if (lastImage == null)
	        {
	        	diffcount = image.getWidth()*image.getHeight();
	        	return dif;    
	        }
	      
	        
	        int[] cpixels = ((DataBufferInt) dif.getRaster().getDataBuffer()).getData();
	        int[] spixels;
	        
	        if (lastImage.getWidth() == image.getWidth())
	        	spixels = ((DataBufferInt) lastImage.getRaster().getDataBuffer()).getData();
	        else
	        {
	        	BufferedImage im2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
	        	im2.createGraphics().drawImage(lastImage, 0, 0, image.getWidth()-1, image.getHeight()-1, 0, 0, lastImage.getWidth()-1, lastImage.getHeight()-1, null);
	        	spixels = ((DataBufferInt) im2.getRaster().getDataBuffer()).getData();
	        }

	        int[] calpha = ((DataBufferInt) dif.getAlphaRaster().getDataBuffer()).getData();
	          	        
	        for (int i = 0; i < cpixels.length; i += 1)
	        {
	                int r1 = (cpixels[i]) & 0xFF;
	                int g1 = (cpixels[i] >> 8) & 0xFF;
	                int b1 = (cpixels[i] >> 16) & 0xFF;
	                int r2 = (spixels[i]) & 0xFF;
	                int g2 = (spixels[i] >> 8) & 0xFF;
	                int b2 = (spixels[i] >> 16) & 0xFF;
	                
	               
	                if (r1 == r2 && g1 == g2 && b1 == b2)
	                {
	                    cpixels[i] = 0;
	                    calpha[i] = 0;
	                }
	                else
	                	diffcount++;
	                
	        }
	        
	        System.out.println("diffcount ---- " + diffcount + " ;   time = " +  (new Date().getTime()-ttt));
	        
	        return dif;
	}
	
	private BufferedImage resizeImage(BufferedImage im, double f)
	{
		BufferedImage res = new BufferedImage((int)(im.getWidth()*f), (int)(im.getHeight()*f), BufferedImage.TYPE_INT_ARGB);
		res.createGraphics().drawImage(im, 0, 0, res.getWidth()-1, res.getHeight()-1, 0, 0, im.getWidth()-1, im.getHeight()-1, null);
		return res;
	}
	
	private BufferedImage[][] tileImage(BufferedImage image, int tileX, int tileY, boolean extraWidth)
	{
		BufferedImage[][] tiles = new BufferedImage[tileX][];
		int tileWidth = image.getWidth()/tileX;
		int tileHeight = image.getHeight()/tileY;
		if (extraWidth) tileWidth += 1;
		for (int i=0; i<tileX; i++)
		{
			tiles[i] = new BufferedImage[tileY];
			for (int j=0; j<tileY; j++)
			{
				tiles[i][j] = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
				tiles[i][j].createGraphics().drawImage(image, 0, 0, tileWidth, tileHeight,  i*tileWidth, j*tileHeight, (i+1)*tileWidth, (j+1)*tileHeight, null);
			}
		}
		
		return tiles;
	}

}
