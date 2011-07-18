package com.spp.lab.alife;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.*;

import java.lang.reflect.Array;
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
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        lifeView = new LifeRenderView(this);
        setContentView(lifeView);
    }

    @Override
    protected void onResume(){
        super.onResume();
        lifeView.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        lifeView.pause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gol_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case(R.id.reset):
                lifeView.initializeBoard();
                return true;
            case(R.id.pause):
                lifeView.pause();
                return true;
            case(R.id.resume):
                lifeView.resume();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //actual GOL implementation
    class LifeRenderView extends SurfaceView implements Runnable{
        boolean [][] board;
        boolean [][] tempBoard;
        volatile boolean running = false;
        Thread renderThread = null;
        SurfaceHolder holder;
        long stepTime = 175; //the target time for each step of the simulation in ms
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
                long start = System.currentTimeMillis();
                Canvas canvas = holder.lockCanvas();
                updateState();
                updateBoard();
                renderBoard(canvas);
                holder.unlockCanvasAndPost(canvas);
                long delta = System.currentTimeMillis()-start;
                StringBuilder s = new StringBuilder();
                s.append(delta);
                Log.d("delta", s.toString());
                try {
                    long sleepTime = stepTime-delta;
                    if (sleepTime<0)
                        sleepTime = 0;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
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
            if (runningState)
                pause();
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            xSize = display.getWidth()/cellSize;
            ySize = display.getHeight()/cellSize;
            board = new boolean[ySize][xSize];
            tempBoard = new boolean[ySize][xSize];

            Random rand = new Random();
            for (int y=0; y<ySize; y++){
                for (int x=0; x<xSize; x++){
                    board[y][x] = rand.nextInt() % 7 == 0;
                }
            }
            if (runningState)
                resume();
        }

        private void initTest(){
            xSize = 10;
            ySize = 10;
            board = new boolean[ySize][xSize];
            tempBoard = new boolean[ySize][xSize];
            for (int x=0; x<xSize; x++){
                board[0][x] = x%2==0;
            }
            for (int y=1; y<ySize; y++){
                for (int x=0; x<xSize; x++){
                    board[y][x]= !board[y-1][x];
                }
            }
        }

        private void renderBoard(Canvas canvas){
            Paint paint = new Paint();
            paint.setARGB(255, 0, 255, 0); paint.setStyle(Paint.Style.FILL);
            canvas.drawColor(Color.BLACK);
            for (int y=0; y<ySize; y++){
                for (int x=0; x<xSize; x++){
                    if (board[y][x]){
                        int xTMP = x*cellSize;
                        int yTMP = y*cellSize;
                        canvas.drawRect(xTMP, yTMP, xTMP+cellSize, yTMP+cellSize, paint);
                    }
                }
            }
        }

        private void updateBoard(){
            for (int y=0; y<ySize; y++){
                System.arraycopy(tempBoard[y], 0, board[y], 0, xSize);
            }
        }

        private void updateState(){
            int tmpy[] = new int[2];
            int tmpx[] = new int[2];
            int neighbors;

            for (int y=0; y<ySize; y++){
                //get temporary values (wrap around to the other side at edge)
                if (y - 1 < 0) {
                    tmpy[0] = ySize - 1;
                } else {
                    tmpy[0] = y - 1;
                }
                if (y + 1 == ySize) {
                    tmpy[1] = 0;
                } else {
                    tmpy[1] = y + 1;
                }
                for (int x=0; x<xSize; x++){
                    if (x - 1 < 0) {
                        tmpx[0] = xSize - 1;
                    } else {
                        tmpx[0] = x - 1;
                    }
                    if (x + 1 == xSize) {
                        tmpx[1] = 0;
                    } else {
                        tmpx[1] = x + 1;
                    }
                     /*locations to check for living cells

                       tmpx[0],tmpy[1] |   x,tmpy[1]   |   tmpx[1],tmpy[1]
                    --------------------------------------------------------------------------
                        tmpx[0],y      |   THIS CELL   |   tmpx[1],y
                    --------------------------------------------------------------------------
                       tmpx[0],tmpy[0] |   x,tmpy[0]   |   tmpx[1],tmpy[0]
                    */

                    //check all the neighboring cells for life...
                    neighbors = 0;
                    neighbors += board[tmpy[1]][tmpx[0]] ? 1:0;
                    neighbors += board[tmpy[1]][x] ? 1:0;
                    neighbors += board[tmpy[1]][tmpx[1]] ? 1:0;
                    neighbors += board[y][tmpx[0]] ? 1:0;
                    neighbors += board[y][tmpx[1]] ? 1:0;
                    neighbors += board[tmpy[0]][tmpx[0]] ? 1:0;
                    neighbors += board[tmpy[0]][x] ? 1:0;
                    neighbors += board[tmpy[0]][tmpx[1]] ? 1:0;

                    tempBoard[y][x] = determineState(neighbors, board[y][x]);
                }
            }
        }


        private boolean determineState(int n, boolean current){
            return ((n==2&&current)||n==3);
        }
    }
}