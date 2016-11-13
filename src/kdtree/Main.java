package kdtree;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Main 
{
	public static void main(String[] args)
    {
        System.out.println("Entrer le nom de l'image à charger :");
        String filename = new Scanner(System.in).nextLine();
        
        try{
            File pathToFile = new File(filename);
            BufferedImage img = ImageIO.read(pathToFile);

            int imgHeight = img.getHeight();
            int imgWidth  = img.getWidth();
            BufferedImage res_img = new BufferedImage(imgWidth, imgHeight, img.getType());
            BufferedImage id_img = new BufferedImage(imgWidth, imgHeight, img.getType());

/////////////////////////////////////////////////////////////////
//TODO: replace this naive image copy by the quantization
/////////////////////////////////////////////////////////////////

            ArrayList<Point3i> pixelColor=new ArrayList<>();
            int max_depth = 4;
            for (int y = 0; y < imgHeight; y++) {
                for (int x = 0; x < imgWidth; x++) {
                    int Color = img.getRGB(x,y);
                    int R = (Color >> 16) & 0xff;
                    int G = (Color >> 8) & 0xff;
                    int B = Color & 0xff;
                    pixelColor.add(new Point3i(R, G, B));


                    /*
                    int resR = R, resG = G, resB = B;

                    int cRes = 0xff000000 | (resR << 16)
                                          | (resG << 8)
                                          | resB;
                    res_img.setRGB(x,y,cRes);
                    */
                }
            }
            //Creation of the tree
            KdTree<Point3i> tree = new KdTree<Point3i>(3,pixelColor,1<<max_depth);

            // Get the color of the palette
            ArrayList<Point3i> palette_colors = new ArrayList<Point3i>();
            tree.getPointsFromLeaf(palette_colors);
                //creation of a second tree to now which color of the palette you want to use
            ArrayList<RefColor> tmp_palette = new ArrayList<RefColor>(1<<max_depth);
            int i = 0;
            for(Point3i p : palette_colors) {
                tmp_palette.add(new RefColor(p,i));
                System.out.println(i);
                ++i;

            }
            KdTree<RefColor> paletteTree = new KdTree<RefColor>(3,tmp_palette,1<<max_depth);
            int v_id[] = new int[imgHeight*imgWidth];
            for( int j = 0 ; j < pixelColor.size() ; j++){
                RefColor rColor = paletteTree.getNN(new RefColor(pixelColor.get(j), 1));
                v_id[j] = rColor.getId() < 0 ? 0 : rColor.getId();
                System.out.println(v_id[j]);
            }

            for (int y = 0; y < imgHeight; y++) {
                for (int x = 0; x < imgWidth; x++) {
                    int id = v_id[y*imgWidth+x]; // Get id of new color for current pixel
                    Point3i color = palette_colors.get(id);  // Get color from id
                    int resR = color.get(0), resG =  color.get(1), resB =  color.get(2);
                    int cRes = 0xff000000 | (resR << 16)
                                          | (resG << 8)
                                          | resB;
                    res_img.setRGB(x,y,cRes);
                    id_img.setRGB(x,y,id);
                }
            }
/////////////////////////////////////////////////////////////////

            ImageIO.write(id_img, "jpg", new File("ResId.jpg"));
            ImageIO.write(res_img, "jpg", new File("ResColor.jpg"));
/////////////////////////////////////////////////////////////////
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
