package com.example.mazma.a4096_5x5;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.widget.TextView;

class SquareObj {

    private TextView view;
    private Context context;
    private int number;
    private boolean isCoolided;

    SquareObj(Context context, int number) {
        this.context = context;
        this.number = number;
        view = new TextView(context);
        view.setTextSize(30);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setGravity(Gravity.CENTER);
        this.setColor(number);
        this.isCoolided = false;
    }

    TextView getView() {
        return this.view;
    }


    int getNumber() {
        return this.number;
    }

    void setNumber(int number) {
        this.view.setText(String.valueOf(number));
        if(number > 1000) {
            view.setTextSize(20);
        } else if(number == 0) {
            this.view.setText("");
        }
        this.setColor(number);
        this.number = number;
    }

    boolean getIsCoolided() {
        return this.isCoolided;
    }

    void setColided(boolean isCoolided) {
        this.isCoolided = isCoolided;
    }

    private void setColor(int number) {
        int color;
        Resources resource = context.getResources();
        switch (number) {
            case 2:
                color = ResourcesCompat.getColor(resource, R.color.square2, null);
                break;
            case 4:
                color = ResourcesCompat.getColor(resource, R.color.square4, null);
                break;
            case 8:
                color = ResourcesCompat.getColor(resource, R.color.square8, null);
                break;
            case 16:
                color = ResourcesCompat.getColor(resource, R.color.square16, null);
                break;
            case 32:
                color = ResourcesCompat.getColor(resource, R.color.square32, null);
                break;
            case 64:
                color = ResourcesCompat.getColor(resource, R.color.square64, null);
                break;
            case 128:
                color = ResourcesCompat.getColor(resource, R.color.square128, null);
                break;
            case 256:
                color = ResourcesCompat.getColor(resource, R.color.square256, null);
                break;
            case 512:
                color = ResourcesCompat.getColor(resource, R.color.square512, null);
                break;
            case 1024:
                color = ResourcesCompat.getColor(resource, R.color.square1024, null);
                break;
            case 2048:
                color = ResourcesCompat.getColor(resource, R.color.square2048, null);
                break;
            case 4096:
                color = ResourcesCompat.getColor(resource, R.color.square4096, null);
                break;
            default:
                color = ResourcesCompat.getColor(resource, R.color.squareColor, null);
                break;
        }
        view.setBackgroundColor(color);
    }
}