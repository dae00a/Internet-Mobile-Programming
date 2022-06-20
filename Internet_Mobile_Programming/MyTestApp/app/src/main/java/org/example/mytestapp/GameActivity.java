package org.example.mytestapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final int INIT = 0;
    private static final int START = 1;
    private static final int STOP = 2;

    private static final int HIGH = 20 * 100;
    private static final int MID = 40 * 100;
    private static final int LOW = 60 * 100;

    Handler handler = new Handler();

    TableLayout gameTable;
    Block[][] blocks;
    ImageView emoji;
    TextView flagText;

    DeviceControl dev;

    private final int blockRow = 10;
    private final int blockColumn = 8;
    private final int mineNum = 10;

    private int flagNum;
    private int openBlockNum;

    private int timeState = INIT;
    private static int time;

    private boolean isClear;

    // Used to load the 'mytestapp' library on application startup.
    /*
    static {
        System.loadLibrary("mytestapp");
    }
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game);

        gameTable = (TableLayout)findViewById(R.id.game_table);

        emoji = findViewById(R.id.emoji_view);
        flagText = findViewById(R.id.flag_text_view);

        dev = new DeviceControl();

        startGame();
        // Example of a call to a native method
    }

    @Override
    protected void onDestroy() {
        timeState = INIT;
        dev.textClear();

        super.onDestroy();
    }

    private void startGame() {
        flagNum = mineNum;
        openBlockNum = blockRow * blockColumn;

        gameTable.removeAllViews();

        Glide.with(this).load(R.raw.happy).into(emoji);
        flagText.setText(String.valueOf(flagNum));

        dev.textClear();
        createTable();
        showTable();
        startTimer();
    }

    private void createTable() {
        blocks = new Block[blockRow][blockColumn];

        for (int row = 0; row < blockRow; row++) {
            for (int column = 0; column < blockColumn; column++) {
                blocks[row][column] = new Block(this);
                blocks[row][column].setDefault();

                final int curRow = row;
                final int curColumn = column;

                blocks[row][column].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (blocks[curRow][curColumn].isBlocked() && !blocks[curRow][curColumn].isFlagged()) {
                            if (blocks[curRow][curColumn].isMined())
                                failGame();

                            openBlocks(curRow, curColumn);

                            if (checkClear())
                                clearGame();
                        }
                    }
                });

                blocks[row][column].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (blocks[curRow][curColumn].isBlocked() && blocks[curRow][curColumn].isFlagged() && flagNum < mineNum) {
                            blocks[curRow][curColumn].cancelFlag();
                            flagNum++;
                            flagText.setText(String.valueOf(flagNum));
                            return true;
                        }
                        else if (blocks[curRow][curColumn].isBlocked() && !blocks[curRow][curColumn].isFlagged() && flagNum > 0) {
                            blocks[curRow][curColumn].setFlag();
                            flagNum--;
                            flagText.setText(String.valueOf(flagNum));
                            return true;
                        }
                        else
                            return false;
                    }
                });
            }
        }

        setMines();
        setNearMineNum();
    }

    private void showTable() {
        for (int row = 0; row < blockRow; row++) {
            TableRow tableRow = new TableRow(this);
            //tableRow.setLayoutParams(new LayoutParams(blockSize * blockColumn, blockSize));
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));

            for (int column = 0; column < blockColumn; column++) {
                //blocks[row][column].setLayoutParams(new LayoutParams(blockSize, blockSize));
                blocks[row][column].setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
                tableRow.addView(blocks[row][column]);
            }
            //gameTable.addView(tableRow, new TableLayout.LayoutParams(blockSize * blockColumn, blockSize * blockRow));
            gameTable.addView(tableRow, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        }
    }

    private void openBlocks(int clickedRow, int clickedCol) {
        if (blocks[clickedRow][clickedCol].isFlagged()) {
            flagNum++;
            flagText.setText(String.valueOf(flagNum));
        }
        if (blocks[clickedRow][clickedCol].openBlock())
            openBlockNum--;
        if (blocks[clickedRow][clickedCol].getSurroundMineNum() > 0)
            return;
        for (int row = -1; row < 2; row++) {
            for (int column = -1; column < 2; column++) {
                // check all the above checked conditions
                // if met then open subsequent blocks
                if ((row + clickedRow >= 0) && (row + clickedRow < blockRow)
                        && (column + clickedCol >= 0) && (column + clickedCol < blockColumn)
                        && blocks[clickedRow + row][clickedCol + column].isBlocked()) {
                    openBlocks(clickedRow + row, clickedCol + column);
                }
            }
        }
    }

    private void setMines() {
        // set mines excluding the location where user clicked
        int mineRow, mineColumn;
        Random rand = new Random(System.nanoTime());

        for (int i = 0; i < mineNum; i++) {
            mineRow = rand.nextInt(blockRow);
            mineColumn = rand.nextInt(blockColumn);

            if (blocks[mineRow][mineColumn].isMined()) // mine is already there, don't repeat for same block
                i--;
            else // plant mine at this location
                blocks[mineRow][mineColumn].setMine();
        }
    }

    private void setNearMineNum() {
        int nearMineCount;
        // count the number of mines in surrounding blocks
        for (int row = 0; row < blockRow; row++) {
            for (int column = 0; column < blockColumn; column++) {
                // for each block find nearby mine count
                nearMineCount = 0;
                // check in all nearby blocks
                for (int previousRow = -1; previousRow < 2; previousRow++) {
                    for (int previousColumn = -1; previousColumn < 2; previousColumn++) {
                        if ((row + previousRow >= 0) && (row + previousRow < blockRow)
                                && (column + previousColumn >= 0) && (column + previousColumn < blockColumn)
                                && blocks[row + previousRow][column + previousColumn].isMined()) {
                            // a mine was found so increment the counter
                            nearMineCount++;
                        }
                    }
                }
                blocks[row][column].setSurroundMineNum(nearMineCount);
            }
        }
    }

    private void failGame() {
        timeState = STOP;
        isClear = false;

        Glide.with(this).load(R.raw.cry).into(emoji);
        openAllMines();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setMessage("You Fail!");
        builder.setIcon(null);
        builder.setCancelable(false);

        builder.setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                timeState = INIT;
                startGame();
            }
        });

        builder.setNegativeButton("BACK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                timeState = INIT;
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(500);

        TextLcdThread textLcdThread = new TextLcdThread();
        textLcdThread.start();
    }

    private boolean checkClear() {
        return (openBlockNum <= mineNum);
    }

    private void clearGame() {
        timeState = STOP;
        isClear = true;

        openAllMines();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setMessage("You Clear!");
        builder.setIcon(null);
        builder.setCancelable(false);

        builder.setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                timeState = INIT;
                startGame();
            }
        });

        builder.setNegativeButton("BACK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                timeState = INIT;
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        LedThread ledThread = new LedThread();
        ledThread.start();

        DotMatrixThread dotMatrixThread = new DotMatrixThread();
        dotMatrixThread.start();

        TextLcdThread textLcdThread = new TextLcdThread();
        textLcdThread.start();
    }

    private void openAllMines() {
        for (int row = 0; row < blockRow; row++) {
            for (int column = 0; column < blockColumn; column++) {
                if (blocks[row][column].isMined())
                    blocks[row][column].openBlock();
            }
        }
    }

    private void startTimer() {
        timeState = START;
        TimeThread timeThread = new TimeThread();
        timeThread.start();
    }

    class TextLcdThread extends Thread {
        @Override
        public void run() {
            dev.textWrite(isClear);
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class LedThread extends Thread {
        @Override
        public void run() {
            dev.ledWrite();
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class DotMatrixThread extends Thread {
        int temp = 0;

        @Override
        public void run() {
            while(timeState == STOP) {
                if (time < HIGH)
                    dev.dotMatrixWrite("A");
                else if (time < MID)
                    dev.dotMatrixWrite("B");
                else if (time < LOW)
                    dev.dotMatrixWrite("C");
                temp++;
                if (temp == 100) {
                    while (temp > 0) {
                        dev.dotMatrixWrite("0");
                        temp--;
                    }
                }
            }
            try {
                dev.dotMatrixWrite("0");
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            time = 0;

            dev.segmentIOctl(1);
            while(timeState != INIT && time <= LOW) {
                dev.segmentWrite(time);

                switch (time) {
                    case HIGH:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(getApplicationContext()).load(R.raw.neutral).into(emoji);
                            }
                        });
                        break;
                    case MID:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(getApplicationContext()).load(R.raw.cry).into(emoji);
                            }
                        });
                        break;
                    case LOW:
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                failGame();
                            }
                        });
                        break;
                    default:
                        break;
                }

                if (timeState == START)
                    time++;
            }
            try {
                dev.segmentWrite(0);
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

