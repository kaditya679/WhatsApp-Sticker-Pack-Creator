package milanesa.stickerpackcreator.main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//Milanesa-chan (@lucc22221) made this crap.

public class Main {
    private static String packName, assetsFolderPath;
    public static String jarPath;

    public static void main(String[] args){
        //Initialization, the packName variable holds the name to put later on on the "contents.json" file
        System.out.println("Developer: Milanesa-chan (@lucc22221)");
        System.out.print("Set a name for your pack: ");
        Scanner inputScanner = new Scanner(System.in);
        packName = inputScanner.next();

        //Get the path of folders accessed frequently. The absolute path of the jar is needed
        //since many instances of this app will be executing at the same time.
        jarPath = FileGetters.getJarPath();
        assetsFolderPath = jarPath.concat("/android/app/src/main/assets");

        //This makes sure every directory needed is created and is empty.
        //Beware everything from the input/resized/converted folders and also the assets folder of the
        //android app will be deleted, so don't put your non-backup'd family photos in there!
        checkFolderConditions(jarPath);

        //Sticker images from the input folder are resized and put into "resized". I hope i've been clear they are resized.
        outputResizedImages(jarPath, ImageModifiers.resizeAllImages(FileGetters.getImagesFromFolder(jarPath)));

        //Everything from the resized folder is put into the converted folder in webp format.
        //Don't put your hand in there or else it will be compressed into webp.
        //Worry not, webp is open source, you can get it back.
        ImageModifiers.convertImagesToWebp(jarPath);
        ImageModifiers.moveImagesToAssets(assetsFolderPath, jarPath);

        //The tray image has a different size and is not compressed as the sticker ones,
        //So it has to be done on a different process.
        BufferedImage trayImage = FileGetters.getTrayImage(jarPath);
        trayImage = ImageModifiers.resizeTray(trayImage);
        outputTrayImage(assetsFolderPath, trayImage);

        //We get the data from a model json in the same folder as the jar. No, don't modify the model.
        //Yes I could have put it inside the jar but me don't like that.
        List<String> jsonData = FileGetters.getModelData(FileGetters.getModelJson(jarPath));

        //It's a weird name but the "custom" json is the model json with the data specifically needed for
        //this pack regex'd into it.
        List<String> customJsonData = FileModifiers.customizeDataForPack(jsonData, packName, "tray.png");
        FileModifiers.writeContentsJson(assetsFolderPath, customJsonData);

        //Start the build of the apk. It uses the gradle "assembleDebug" I plan on making this better in the future,
        //but for now this will work. Also the output apk gets moved into the "output" folder.
        FileModifiers.startGradleBuild(jarPath);
        String apkOutputPath = jarPath.concat("/android/app/build/outputs/apk/debug/app-debug.apk");
        moveApkToOutput(jarPath, apkOutputPath);
        System.out.println("[Main] Process finished.");
    }

    private static void checkFolderConditions(String mainDirPath){
        File inputFolder = new File(mainDirPath.concat("/input"));
        File resizedFolder = new File(mainDirPath.concat("/resized"));
        File convertedFolder = new File(mainDirPath.concat("/converted"));
        File outputFolder = new File(mainDirPath.concat("/output"));

        if(!inputFolder.exists() || !inputFolder.isDirectory()){
            inputFolder.mkdirs();
            System.out.println("[Error][checkFolderConditions] No input folder found!");
            System.out.println("[checkFolderConditions] Created input folder. Put your images there.");
            Runtime.getRuntime().exit(1);
        }

        if(resizedFolder.exists() && resizedFolder.listFiles().length>0){
            for(File file : resizedFolder.listFiles()){
                file.delete();
            }
            System.out.println("[checkFolderConditions] All files in \"resized\" have been deleted.");
        }

        if(!convertedFolder.exists()){
            System.out.println("[checkFolderConditions] Creating \"converted\" folder.");
            convertedFolder.mkdirs();
        }
        if(convertedFolder.exists() && convertedFolder.listFiles().length>0){
            for(File file : convertedFolder.listFiles()){
                file.delete();
            }
            System.out.println("[checkFolderConditions] All files in \"converted\" have been deleted.");
        }

        if(!outputFolder.exists()){
            System.out.println("[checkFolderConditions] Creating \"output\" folder.");
            outputFolder.mkdirs();
        }
        if(outputFolder.exists() && outputFolder.listFiles().length>0){
            for(File file : outputFolder.listFiles()){
                file.delete();
            }
            System.out.println("[checkFolderConditions] All files in \"output\" have been deleted.");
        }

        FileModifiers.prepareAssetsFolder(assetsFolderPath);
    }

    private static void outputResizedImages(String mainDirPath, BufferedImage[] imagesToOutput){
        File outputFolder = new File(mainDirPath.concat("/resized"));

        if(!outputFolder.exists()){
            outputFolder.mkdir();
        }

        try {
            if (outputFolder.isDirectory()) {
                for (int imageIndex = 0; imageIndex < imagesToOutput.length; imageIndex++) {
                    File imageFile = new File(outputFolder.getAbsolutePath().concat("/" + (imageIndex+1) + ".png"));
                    ImageIO.write(imagesToOutput[imageIndex], "png", imageFile);

                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void outputTrayImage(String assetsFolderPath, BufferedImage trayImage){
        try {
            File outputImageFile = new File(assetsFolderPath.concat("/1/tray.png"));
            ImageIO.write(trayImage, "png", outputImageFile);
            System.out.println("[outputTrayImage] Tray image written into assets folder.");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void moveApkToOutput(String mainDirPath, String apkOutputPath){
        File apkFile = new File(apkOutputPath);
        apkFile.renameTo(new File(mainDirPath.concat("/output/CustomStickerPack.apk")));
        System.out.println("[moveApkToOutput] Apk moved to output folder. Main process finished successfully!");
    }
}
