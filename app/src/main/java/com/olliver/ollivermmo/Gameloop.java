package com.olliver.ollivermmo;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Observer;

public class Gameloop extends Thread{
    private static final double MAX_UPS =60.0;
    private static final double UPS_PERIOD = 1E+3/MAX_UPS;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private Game game;
    private double averageUPS;
    private double averageFPS;

    public Gameloop(Game game, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public void startloop() {
        isRunning=true;
        start();
    }

    @Override
    public void run() {
        super.run();
        //declare time
        int updateCount = 0;
        int frameCount = 0;

        long startTime;
        long elapsedTime;
        long sleepTime;

        //gameloop
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while(isRunning){

            //update render game
            try{
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    game.update();
                    updateCount++;
                    game.draw(canvas);
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }finally {
                if (canvas != null){
                    try{
                        frameCount++;
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            //ups fps game loop
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime =(long) (updateCount*UPS_PERIOD-elapsedTime);
            if(sleepTime>0){
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //skip time to keep with target UPS
            while(sleepTime < 0 && updateCount < MAX_UPS-1){
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime =(long) (updateCount*UPS_PERIOD-elapsedTime);
            }

            //caculate average fps ups
            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime >= 1000){
                averageUPS = updateCount / (1E-3*elapsedTime);
                averageFPS = frameCount / (1E-3*elapsedTime);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }

        }
    }
}
