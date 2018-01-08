package com.demo.jump2;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

import com.demo.jump.ImgLoader;
import com.demo.jump.MyPosFinder;
import com.demo.jump.NextCenterFinder;
import com.demo.jump.WhitePointFinder;

/**
 * author: Stark
 * Date: 2018年1月6日
 * Time: 下午11:12:55
 */
public class RobotTest2{
	static int DELAY = 5;
	static String IMGPATH = "E:\\Workspace\\Demo2\\ScreenShot\\";
    static double JUMP_RATIO = 1.380d;
	
    public static void main(String[] args) throws Exception{
        Robot robot = new Robot();
        robot.setAutoDelay(DELAY);
        String phoneFolder = "/sdcard/jump/";
        String pcFolder = "/jump/";

        for (int i = 0; i < 260; i++) {
//        	BufferedImage bi = getScreenShot(robot, false);
        	
            // 1.截屏 2.复制文件到电脑 3.解析图片 4.执行shell
        	String fileName = "screenshot" + (new Date().getTime() / 1000) + ".png";
        	String shell1 = "/system/bin/screencap -p " + phoneFolder + fileName;
        	pressSceen(robot, shell1, 1000, 1200);
            
        	Thread.sleep(1600);
        	String shell2 = "adb pull " + phoneFolder + fileName + " " +pcFolder + fileName;
        	pressSceen(robot, shell2, 1000, 750);
        	
        	Thread.sleep(1200);
        	BufferedImage image = ImgLoader.load("f:" + pcFolder + fileName);
        	String shell = getNextDelayShell(image);
        	
            pressSceen(robot, shell, 1000, 1200);
            
            System.out.println("===============" + i);
            Thread.sleep(getRandomNum(2800, 3400));
		}
        System.exit(0);
        
        
//        String shell2 = "rm " + phoneFolder;
//    	pressSceen(robot, shell2, 1000, 1200);
    }
    
    private static String getNextDelayShell(BufferedImage img){
    	Random random = new Random();
    	int delay = 0;
    	MyPosFinder mf = new MyPosFinder();
        int pos[] = mf.find(img);
        String adbCommand = "";
        NextCenterFinder nextCenterFinder = new NextCenterFinder();
        WhitePointFinder whitePointFinder = new WhitePointFinder();
        
        Double jump_ratio = JUMP_RATIO * 1080 / img.getWidth();
        int[] nextCenter = nextCenterFinder.find(img, pos);
        if (nextCenter == null || nextCenter[0] == 0) {
            System.err.println("find nextCenter, fail");
        } else {
            int centerX, centerY;
            int[] whitePoint = whitePointFinder.find(img, nextCenter[0] - 120, nextCenter[1], nextCenter[0] + 120, nextCenter[1] + 180);
            if (whitePoint != null) {
                centerX = whitePoint[0];
                centerY = whitePoint[1];
            } else {
                if (nextCenter[2] != Integer.MAX_VALUE && nextCenter[4] != Integer.MIN_VALUE) {
                    centerX = (nextCenter[2] + nextCenter[4]) / 2;
                    centerY = (nextCenter[3] + nextCenter[5]) / 2;
                } else {
                    centerX = nextCenter[0];
                    centerY = nextCenter[1] + 48;
                }
            }
            System.out.println("find nextCenter, succ, (" + centerX + ", " + centerY + ")");
            delay = (int) (Math.sqrt((centerX - pos[0]) * (centerX - pos[0]) + (centerY - pos[1]) * (centerY - pos[1])) * jump_ratio);
//            delay -= 5;
            int pressX = 400 + random.nextInt(100);
            int pressY = 500 + random.nextInt(100);
//            String adbCommand = String.format(" shell input swipe %d %d %d %d %d", pressX, pressY, pressX, pressY, delay);
            adbCommand = String.format("input swipe %d %d %d %d %d", pressX, pressY, pressX, pressY, delay);
            System.out.println("command：" + adbCommand);
        }
        
//        delay -= 731;            
        System.out.println("distance: " + delay);
    	return adbCommand;
    }
    
    private static void pressSceen(Robot robot, final String shell, int windowX, int windowY){
    	robot.mouseMove(windowX, windowY);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        
//        System.out.println("word：" + word);
        for (int i = 0; i < shell.length(); i++) {
			char c = shell.charAt(i);
			robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
            robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(c));
		}
        
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
    
    private static BufferedImage getScreenShot(Robot robot, boolean saveFile) {
    	BufferedImage bufferedImage = null;
    	try {
    		Rectangle screenRect = new Rectangle(15, 36, 720, 1280);
    		bufferedImage = robot.createScreenCapture(screenRect);
    		if(saveFile){
	    		File file = new File(IMGPATH + "input\\screenRect - " + (new Date().getTime() / 1000) + ".png");
	    		ImageIO.write(bufferedImage, "png", file);
    		}
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
    	return bufferedImage;
    }
    

    public static int getRandomNum(int minnum,int maxnum){
        return (int)(Math.random()*(maxnum-minnum+1))+minnum;
    }
}