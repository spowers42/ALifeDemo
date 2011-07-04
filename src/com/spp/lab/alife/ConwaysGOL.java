package com.spp.lab.alife;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.*;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: S. Powers
 * Date: 7/3/11
 * Time: 2:05 PM
 * This is an implementation of John Conways Game of Life simulation.  It will render within
 * an android activity.
 */
public class ConwaysGOL extends Activity {
    LifeRenderView lifeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        lifeView = new LifeRenderView(this);
        setContentView(lifeView);
    }

    protected void onResume(){
        super.onResume();
        lifeView.resume();
    }

    protected void onPause(){
        super.onPause();
        lifeView.pause();
    }

    //actual GOL implementation
    class LifeRenderView extends SurfaceView implements Runnable{
        boolean [][] board;
        boolean [][] tempBoard;
        volatile boolean running = false;
        Thread renderThread = null;
        SurfaceHolder holder;
        int stepTime = 1000; //the target time for each step of the simulation in ms
        //the size of the board
        int xSize;
        int ySize;
        int cellSize = 4;

        public LifeRenderView(Context context){
            super(context);
            holder = getHolder();
            initializeBoard();
        }

        public void resume(){
            running = true;
            renderThread = new Thread(this);
            renderThread.start();
        }

        //run the simulation, this is our actual game control loop
        public void run(){
            while(running){
                if(!holder.getSurface().isValid()){
                    continue;
                }

                Canvas canvas = holder.lockCanvas();
                renderBoard(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
            //todo get start time
            //todo update the temp board
            //todo move temp to real board
            //todo get end time, and time delta
            //todo sleep for target-delta
        }

        //handle the pause of the render thread because the activity is paused
        public void pause(){
            running = false;
            while (true){
                try{
                    renderThread.join();
                    break;
                }catch (InterruptedException e){
                    //NOP
                }
            }
        }

        //Set the initial board conditions to a random state upon creation, or when
        //the simulation is reset
        private void initializeBoard(){
            boolean runningState = running;
            running = false;
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            xSize = display.getWidth()/cellSize;
            ySize = display.getHeight()/cellSize;
            board = new boolean[xSize][ySize];
            tempBoard = new boolean[xSize][ySize];

            Random rand = new Random();
            for (int y=0; y<ySize; y++){
                for (int x=0; x<xSize; x++){
                    board[x][y] = rand.nextBoolean();
                }
            }
            running = runningState;
        }

        private void renderBoard(Canvas canvas){
            Paint paint = new Paint();
            paint.setARGB(255, 0, 255, 0); paint.setStyle(Paint.Style.FILL);
            for (int y=0; y<ySize; y++){
                for (int x=0; x<xSize; x++){
                    if (board[x][y]){
                        int xTMP = x*cellSize;
                        int yTMP = y*cellSize;
                        canvas.drawRect(xTMP, yTMP, xTMP+cellSize, yTMP+cellSize, paint);
                    }
                }
            }
        }

        private void updateState(){

        }

        private boolean determineState(int n, boolean current){
            if ((n==2 && current)||n==3)
                return true;
            else
                return false;
        }
    }
}