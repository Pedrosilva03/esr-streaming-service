package utils;

import java.awt.image.BufferedImage;

public class Extras {
    public static BufferedImage convertYUVtoRGB(BufferedImage yuvImage) {
        if(yuvImage == null) return null;
        int width = yuvImage.getWidth();
        int height = yuvImage.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Criar arrays para armazenar os valores RGB
        int[] rgbPixels = new int[width * height];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yuvPixel = yuvImage.getRGB(x, y);
                
                // Extraindo componentes YUV do pixel
                int yy = (yuvPixel >> 16) & 0xFF;  // Luminância
                int u = (yuvPixel >> 8) & 0xFF;   // Crominância (U)
                int v = yuvPixel & 0xFF;          // Crominância (V)
    
                // Calcular as diferenças uma vez
                int uDiff = u - 128;
                int vDiff = v - 128;
    
                // Convertendo de YUV para RGB
                int r = (int) (yy + 1.402 * vDiff);
                int g = (int) (yy - 0.344136 * uDiff - 0.714136 * vDiff);
                int b = (int) (yy + 1.772 * uDiff);
    
                // Clamping para garantir que os valores estejam no intervalo [0, 255]
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
    
                // Armazenar o pixel RGB no array
                rgbPixels[y * width + x] = (r << 16) | (g << 8) | b;
            }
        }
        
        // Definindo todos os pixels de uma vez
        rgbImage.setRGB(0, 0, width, height, rgbPixels, 0, width);
        
        return rgbImage;
    }
}
