package com.example.mazma.a4096_5x5;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int WINNER_NUMBER_4096 = 4096;
    public static final int ROWS = 5;
    public static final int COLS = 5;
    private SquareObj[][] squareObjs;
    private ViewGroup table;
    private TextView scoreView;
    private int score, lastUpdatedScore;
    private int[][] undoMatrix;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences prefs = getSharedPreferences("Score", MODE_PRIVATE);
        int high_score = prefs.getInt("high_score", 0);
        TextView bestScore = findViewById(R.id.bestScoreTxt);
        bestScore.setText(String.valueOf(high_score));
        squareObjs = new SquareObj[ROWS][COLS];
        Button restartBtn = findViewById(R.id.restartBtn);
        Button undoBtn = findViewById(R.id.undoBtn);
        table = findViewById(R.id.gameBoardLayout);
        int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
        table.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, widthPixels));
        scoreView = findViewById(R.id.scoreTxt);
        table.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

            @Override
            public void onSwipeRight() {
                swipeRight();
            }

            @Override
            public void onSwipeLeft() {
                swipeLeft();
            }

            @Override
            public void onSwipeTop() {
                swipeUp();
            }

            @Override
            public void onSwipeBottom() {
                swipeDown();
            }
        });

        restartBtn.setOnClickListener(this);
        undoBtn.setOnClickListener(this);

        boardInit();
        initFirstStart();
        score = 0;
        lastUpdatedScore = 0;
    }






    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restartBtn:
                restartGame();
                break;
            case R.id.undoBtn:
                undo();
                break;
        }
    }

    private void undo() {
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                squareObjs[i][j].setNumber(undoMatrix[i][j]);
            }
        }
        score = lastUpdatedScore;
        scoreView.setText(String.valueOf(score));
    }

    private void restartGame() {
        TextView best = findViewById(R.id.bestScoreTxt);
        if (Integer.parseInt(best.getText().toString()) < score) {
            best.setText(String.valueOf(score));
            SharedPreferences.Editor edit = getSharedPreferences("Score", MODE_PRIVATE).edit();
            edit.putInt("high_score", score);
            edit.apply();
        }
        score = 0;
        lastUpdatedScore = 0;
        scoreView.setText("0");
        table.removeAllViews();
        boardInit();
        initFirstStart();
        undoMatrix = getCopyMatrix(squareObjs);
    }

    private void swipeDown() {
        new TaskDown().execute();
    }

    private void swipeUp() {
        new TaskUp().execute();
    }

    private void swipeLeft() {
        new TaskLeft().execute();
    }

    private void swipeRight() {
        new TaskRight().execute();
    }

    private int[][] getCopyMatrix(SquareObj[][] objs) {
        int[][] temp = new int[COLS][ROWS];
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                temp[i][j] = objs[i][j].getNumber();
            }
        }
        return temp;
    }

    private void boardInit() {
        LinearLayout v = findViewById(R.id.gameBoardLayout);
        LinearLayout layout;
        LinearLayout.LayoutParams params;
        for (int i = 0; i < ROWS; i++) {
            layout = new LinearLayout(this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            layout.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < COLS; j++) {
                squareObjs[i][j] = new SquareObj(this,0);
                params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                params.setMargins(15, 15, 15, 15);
                squareObjs[i][j].getView().setLayoutParams(params);
                layout.addView(squareObjs[i][j].getView());
            }
            v.addView(layout);
        }
    }

    private void initFirstStart() {
        for (int i = 0; i < 2; i++) {
            addRandomSquare();
        }
    }

    private void addRandomSquare() {
        Random r = new Random();
        int row, col, num;
        do {
            row = r.nextInt(ROWS);
            col = r.nextInt(COLS);
        } while (isExists(row, col));
        if (r.nextInt(ROWS * COLS) % 4 != 0) {      // 75% - 2, 25% - 4
            num = 2;
        } else {
            num = 4;
        }
        squareObjs[row][col].setNumber(num);
        squareObjs[row][col].getView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.show_square));

    }

    private boolean isExists(int row, int col) {
        return !(squareObjs[row][col].getNumber() == 0);
    }


    private boolean checkForGameOver() {
        int origin, right, down;
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                origin = squareObjs[i][j].getNumber();
                if (i < COLS - 1) {
                    down = squareObjs[i + 1][j].getNumber();
                } else {
                    down = 0;
                }
                if (j < ROWS - 1) {
                    right = squareObjs[i][j + 1].getNumber();
                } else {
                    right = 0;
                }
                if (origin == 0 || origin == down || origin  == right) {
                    return false;
                }
            }
        }
        return true;
    }



    private boolean checkIfEqual(int[][] matrix) {
        for (int i = 0; i < COLS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (squareObjs[i][j].getNumber() != matrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /* RIGHT SLIDE ASYNC TASK */
    private class TaskRight extends AsyncTask<Void, Integer, Void> {

        int[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getCopyMatrix(squareObjs);
            lastUpdatedScore = score;
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = COLS - 1; j > 0; j--) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(k, j);

                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1]) && isExists(values[0], values[1] - 1)) {
                squareObjs[values[0]][values[1]].setNumber(squareObjs[values[0]][values[1] - 1].getNumber());
                squareObjs[values[0]][values[1]].setColided(squareObjs[values[0]][values[1] - 1].getIsCoolided());
                squareObjs[values[0]][values[1] - 1].setNumber(0);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_right_anim));
            } else if (isExists(values[0], values[1] - 1) && squareObjs[values[0]][values[1]].getNumber() == squareObjs[values[0]][values[1] - 1].getNumber() &&
                    (!squareObjs[values[0]][values[1]].getIsCoolided() && !squareObjs[values[0]][values[1] - 1].getIsCoolided())) {
                int number = squareObjs[values[0]][values[1] - 1].getNumber() * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                squareObjs[values[0]][values[1]].setColided(true);
                squareObjs[values[0]][values[1]].setNumber(number);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                squareObjs[values[0]][values[1] - 1].setNumber(0);
                if(number == WINNER_NUMBER_4096) {
                    youWin();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!checkIfEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            }
            for(int i = 0; i < COLS; i++) {
                for(int j = 0; j < ROWS; j++) {
                    squareObjs[i][j].setColided(false);
                }
            }
            if(checkForGameOver()) {
                gameOver();
            }
        }}

    /* LEFT SLIDE ASYNC TASK */
    private class TaskLeft extends AsyncTask<Void, Integer, Void> {

        int[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getCopyMatrix(squareObjs);
            lastUpdatedScore = score;
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < COLS - 1; j++) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(k, j);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1]) && isExists(values[0], values[1] + 1)) {
                squareObjs[values[0]][values[1]].setNumber(squareObjs[values[0]][values[1] + 1].getNumber());
                squareObjs[values[0]][values[1]].setColided(squareObjs[values[0]][values[1] + 1].getIsCoolided());
                squareObjs[values[0]][values[1] + 1].setNumber(0);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_left_anim));
            } else if (isExists(values[0], values[1] + 1) && squareObjs[values[0]][values[1]].getNumber() == squareObjs[values[0]][values[1] + 1].getNumber() &&
                    (!squareObjs[values[0]][values[1]].getIsCoolided() && !squareObjs[values[0]][values[1] + 1].getIsCoolided())) {
                int number = squareObjs[values[0]][values[1] + 1].getNumber() * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                squareObjs[values[0]][values[1]].setColided(true);
                squareObjs[values[0]][values[1]].setNumber(number);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                squareObjs[values[0]][values[1] + 1].setNumber(0);
                if(number == WINNER_NUMBER_4096) {
                    youWin();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!checkIfEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            }
            for(int i = 0; i < COLS; i++) {
                for(int j = 0; j < ROWS; j++) {
                    squareObjs[i][j].setColided(false);
                }
            }
            if(checkForGameOver()) {
                gameOver();
            }
        }
    }
    //
    /* UP SLIDE ASYNC TASK */
    private class TaskUp extends AsyncTask<Void, Integer, Void> {

        int[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getCopyMatrix(squareObjs);
            lastUpdatedScore = score;
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = 0; j < ROWS - 1; j++) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(j, k);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1]) && isExists(values[0] + 1, values[1])) {
                squareObjs[values[0]][values[1]].setNumber(squareObjs[values[0] + 1][values[1]].getNumber());
                squareObjs[values[0]][values[1]].setColided(squareObjs[values[0] + 1][values[1]].getIsCoolided());
                squareObjs[values[0] + 1][values[1]].setNumber(0);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_anim));
            } else if (isExists(values[0] + 1, values[1]) && squareObjs[values[0]][values[1]].getNumber() == squareObjs[values[0] + 1][values[1]].getNumber() &&
                    (!squareObjs[values[0]][values[1]].getIsCoolided() && !squareObjs[values[0] + 1][values[1]].getIsCoolided())) {
                int number = squareObjs[values[0] + 1][values[1]].getNumber() * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                squareObjs[values[0]][values[1]].setNumber(number);
                squareObjs[values[0]][values[1]].setColided(true);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                squareObjs[values[0] + 1][values[1]].setNumber(0);
                if(number == WINNER_NUMBER_4096) {
                    youWin();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!checkIfEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            }
            for(int i = 0; i < COLS; i++) {
                for(int j = 0; j < ROWS; j++) {
                    squareObjs[i][j].setColided(false);
                }
            }
            if(checkForGameOver()) {
                gameOver();
            }
        }
    }
    //
    /* DOWN SLIDE ASYNC TASK */
    private class TaskDown extends AsyncTask<Void, Integer, Void> {

        int[][] temp;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            temp = getCopyMatrix(squareObjs);
            lastUpdatedScore = score;
        }

        @Override
        protected Void doInBackground(Void... integers) {
            for (int i = 0; i < COLS; i++) {
                for (int j = ROWS - 1; j > 0; j--) {
                    for (int k = 0; k < COLS; k++) {
                        publishProgress(j, k);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            if (!isExists(values[0], values[1]) && isExists(values[0] - 1, values[1])) {
                squareObjs[values[0]][values[1]].setNumber(squareObjs[values[0] - 1][values[1]].getNumber());
                squareObjs[values[0]][values[1]].setColided(squareObjs[values[0] - 1][values[1]].getIsCoolided());
                squareObjs[values[0] - 1][values[1]].setNumber(0);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down_anim));
            } else if (isExists(values[0] - 1, values[1]) && squareObjs[values[0]][values[1]].getNumber() == squareObjs[values[0] - 1][values[1]].getNumber() &&
                    (!squareObjs[values[0]][values[1]].getIsCoolided() && !squareObjs[values[0] - 1][values[1]].getIsCoolided())) {
                int number = squareObjs[values[0] - 1][values[1]].getNumber() * 2;
                score += number;
                scoreView.setText(String.valueOf(score));
                squareObjs[values[0]][values[1]].setColided(true);
                squareObjs[values[0]][values[1]].setNumber(number);
                squareObjs[values[0]][values[1]].getView().startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.colition));
                squareObjs[values[0] - 1][values[1]].setNumber(0);
                if(number == WINNER_NUMBER_4096) {
                    youWin();
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!checkIfEqual(temp)) {
                addRandomSquare();
                undoMatrix = temp;
            }
            for(int i = 0; i < COLS; i++) {
                for(int j = 0; j < ROWS; j++) {
                    squareObjs[i][j].setColided(false);
                }
            }
            if(checkForGameOver()) {
                gameOver();
            }

        }
    }

    private void gameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setMessage("Game Over! Would you try again?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void youWin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setMessage("Congratulations, You reached 4096!!! You are a real game master. Do you want to continue?").setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton("New Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}