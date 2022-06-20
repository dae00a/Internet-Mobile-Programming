package org.example.mytestapp;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;

public class Block extends AppCompatImageView {
    private boolean isBlocked;
    private boolean isMined;
    private boolean isFlagged;
    private int surroundMineNum;

    public Block(Context context) {
        super(context);
    }

    protected void setDefault() {
        isBlocked = true;
        isMined = false;
        isFlagged = false;
        surroundMineNum = 0;

        this.setBackgroundResource(R.drawable.block);
    }

    protected void viewMine() {
        this.setBackgroundResource(R.drawable.stone);

        this.setImageResource(R.drawable.mine);
    }

    protected void setFlag() {
        isFlagged = true;
        this.setImageResource(R.drawable.flag);
    }

    protected void cancelFlag() {
        isFlagged = false;
        this.setImageResource(0);
    }

    protected void setSurroundMineNum(int num) {
        surroundMineNum = num;
    }

    protected void viewSurroundMineNum(int num) {
        this.setBackgroundResource(R.drawable.stone);

        switch (num) {
            case 1:
                this.setImageResource(R.drawable.one);
                break;
            case 2:
                this.setImageResource(R.drawable.two);
                break;
            case 3:
                this.setImageResource(R.drawable.three);
                break;
            case 4:
                this.setImageResource(R.drawable.four);
                break;
            case 5:
                this.setImageResource(R.drawable.five);
                break;
            case 6:
                this.setImageResource(R.drawable.six);
                break;
            case 7:
                this.setImageResource(R.drawable.seven);
                break;
            case 8:
                this.setImageResource(R.drawable.eight);
                break;
            default:
                break;
        }
    }

    protected boolean openBlock() {
        if (isBlocked) {
            isBlocked = false;

            if (isMined) // check if it has mine
                viewMine();
            else {// update with the nearby mine count
                if (isFlagged)
                    cancelFlag();
                viewSurroundMineNum(surroundMineNum);
            }
            return true;
        }
        else
            return false;
    }

    protected void setMine() {
        isMined = true;
    }

    protected boolean isBlocked() {
        return isBlocked;
    }

    protected boolean isMined() {
        return isMined;
    }

    protected boolean isFlagged() {
        return isFlagged;
    }

    protected int getSurroundMineNum() {
        return surroundMineNum;
    }
}
