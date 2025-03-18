package com.bicycle.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CodeTransfer {

    public static void main(String[] args) throws IOException {
        final Path sourcePath = Paths.get("C:\\data\\bicycle.zip");
        final Path destinationPath = Paths.get("C:\\data\\bicycle.png");
        write(sourcePath, destinationPath);
        read(destinationPath);
    }
    
    private static void read(Path destinationPath) throws IOException {
        final BufferedImage image = ImageIO.read(destinationPath.toFile());
        final DataBufferByte dataBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        final byte[] imageData = dataBuffer.getData();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(imageData);
        final int fileSize = byteBuffer.getInt();
        final byte[] fileData = new byte[fileSize];
        byteBuffer.get(fileData);
        Files.write(Paths.get("C:\\data\\bicycle1.zip"), fileData, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    
    private static void write(Path sourcePath, Path destinationPath) throws IOException {
        final byte[] fileData = Files.readAllBytes(sourcePath);
        final int dim = (int) (Math.sqrt(fileData.length) + 2);
        
        final BufferedImage image = new BufferedImage(dim, dim, BufferedImage.TYPE_BYTE_GRAY);
        final DataBufferByte dataBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
        final byte[] imageData = dataBuffer.getData();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(imageData);
        byteBuffer.putInt(fileData.length);
        byteBuffer.put(fileData);
        image.flush();
        
        ImageIO.write(image, "png", destinationPath.toFile());
    }

}
