/*
 This file is part of Privacy Friendly Minesweeper.

 Privacy Friendly Minesweeper is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Minesweeper is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Minesweeper. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlyminesweeper.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlyminesweeper.R;
import org.secuso.privacyfriendlyminesweeper.activities.adapter.PlayRecyclerViewAdapter;
import org.secuso.privacyfriendlyminesweeper.activities.helper.CellView;
import org.secuso.privacyfriendlyminesweeper.database.DatabaseBestTimeReader;
import org.secuso.privacyfriendlyminesweeper.database.DatabaseBestTimeReader.BestTimeReaderReceiver;
import org.secuso.privacyfriendlyminesweeper.database.DatabaseSavedGameProvide;
import org.secuso.privacyfriendlyminesweeper.database.DatabaseSavedGameWriter;
import org.secuso.privacyfriendlyminesweeper.database.DatabaseWriter;
import org.secuso.privacyfriendlyminesweeper.database.PFMSQLiteHelper;
import org.secuso.privacyfriendlyminesweeper.minesweeper.MineSweeper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author I3ananas, max-dreger
 * @version 20180809
 * This class implements all functions required to handle the process of playing:
 * - creation of the playing field and its content
 * - uncovering fields
 * - check for victory and/or defeat
 * - save statistics on victory and/or defeat
 * - save games
 * - load saved games
 */
public class PlayActivity extends AppCompatActivity implements PlayRecyclerViewAdapter.ItemClickListener, BestTimeReaderReceiver {
    final MineSweeper mineSweeper = new MineSweeper();
    PlayRecyclerViewAdapter adapter;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    int maxHeight;
    boolean firstTime;
    TextView mines;
    boolean firstClick;
    Bundle parameter;
    Chronometer timer;
    DatabaseBestTimeReader bestTimeReader;
    DatabaseWriter writer;
    int bestTime;
    boolean newBestTime;
    boolean savecheck;
    String savedContent;
    String savedStatus;
    int totalSavedSeconds;
    Toolbar toolbar;
    Handler handler;
    boolean savedinstancestate;
    int desired_width;
    boolean game_saved;
    boolean landscape;

    protected void onCreate(Bundle param){
        super.onCreate(param);
        setContentView(R.layout.activity_play);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mineSweeper.setNumberOfColumns(0);
        mineSweeper.setNumberOfRows(0);
        mineSweeper.setNumberOfBombs(0);

        landscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        newBestTime = false;
        mineSweeper.setGameEnded(false);
        game_saved = false;

        //check if this is loading a saved game
        parameter = this.getIntent().getExtras();
        savecheck = false;
        savecheck = parameter.getBoolean("continue");
        //get the game mode and playingfield size
        if (savecheck){
            ArrayList<String> savedInfo = parameter.getStringArrayList("information");
            int id = Integer.valueOf(savedInfo.get(0));
            String savedGameMode = savedInfo.get(1);
            String time = savedInfo.get(2);
            savedContent = savedInfo.get(5);
            savedStatus = savedInfo.get(6);

            if (savedGameMode.equalsIgnoreCase("easy")) {
                mineSweeper.setGame_mode("easy");
                parameter.putShortArray("info", new short[]{(short)6, (short)10, (short)7});
                parameter.putBoolean("continue", false);
                mineSweeper.setNumberOfColumns(6);
                mineSweeper.setNumberOfRows(10);
                mineSweeper.setNumberOfBombs(7);
            } else if (savedGameMode.equalsIgnoreCase("medium")) {
                mineSweeper.setGame_mode("medium");
                parameter.putShortArray("info", new short[]{(short)10, (short)16, (short)24});
                parameter.putBoolean("continue", false);
                mineSweeper.setNumberOfColumns(10);
                mineSweeper.setNumberOfRows(16);
                mineSweeper.setNumberOfBombs(24);
            } else {
                mineSweeper.setGame_mode("difficult");
                parameter.putShortArray("info", new short[]{(short)12, (short)19, (short)46});
                parameter.putBoolean("continue", false);
                mineSweeper.setNumberOfColumns(12);
                mineSweeper.setNumberOfRows(19);
                mineSweeper.setNumberOfBombs(46);
            }

            //handle the saved time
            String[] units = time.split(":");
            int minutes = Integer.parseInt(units[0]);
            int seconds = Integer.parseInt(units[1]);
            totalSavedSeconds = 60 * minutes + seconds;

            DatabaseSavedGameProvide provider = new DatabaseSavedGameProvide(new PFMSQLiteHelper(getApplicationContext()));
            provider.execute(id);
        }
        //get game mode and PlayingField size if this is not loading a saved game
        else {
            short[] test = parameter.getShortArray("info");
            mineSweeper.setNumberOfColumns(test[0]);
            mineSweeper.setNumberOfRows(test[1]);
            mineSweeper.setNumberOfBombs(test[2]);

            if((mineSweeper.getNumberOfColumns() == 6) && (mineSweeper.getNumberOfRows() == 10) && (mineSweeper.getNumberOfBombs() == 7)){
                mineSweeper.setGame_mode("easy");
            }
            else if((mineSweeper.getNumberOfColumns() == 10) && (mineSweeper.getNumberOfRows() == 16) && (mineSweeper.getNumberOfBombs() == 24)){
                mineSweeper.setGame_mode("medium");
            }
            else if((mineSweeper.getNumberOfColumns() == 12) && (mineSweeper.getNumberOfRows() == 19) && (mineSweeper.getNumberOfBombs() == 46)){
                mineSweeper.setGame_mode("difficult");
            }
            else{
                mineSweeper.setGame_mode("user-defined");
            }
        }

        //handle the custom toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_play);
        if(getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            TextView text_game_mode = (TextView) toolbar.findViewById(R.id.game_mode);
            if(mineSweeper.getGame_mode().equals("easy")){
                text_game_mode.setText(getResources().getString(R.string.game_mode_easy));
            }
            if(mineSweeper.getGame_mode().equals("medium")){
                text_game_mode.setText(getResources().getString(R.string.game_mode_medium));
            }
            if(mineSweeper.getGame_mode().equals("difficult")){
                text_game_mode.setText(getResources().getString(R.string.game_mode_difficult));
            }
            if(mineSweeper.getGame_mode().equals("user-defined")){
                text_game_mode.setText(getResources().getString(R.string.game_mode_user_defined_2lines));
            }
        }

        //Creating the right sized the PlayingField
        mineSweeper.setNumberOfCells(mineSweeper.getNumberOfRows() * mineSweeper.getNumberOfColumns());
        mineSweeper.setData(new int[mineSweeper.getNumberOfCells()]);
        mineSweeper.setCountDownToWin(mineSweeper.getNumberOfCells());

        //status saves the state of the cell
        //0 = normal, 1 = revealed, 2 = marked
        mineSweeper.setStatus(new int[mineSweeper.getNumberOfCells()]);
        for (int i = 0; i < mineSweeper.getNumberOfCells(); i++) {
            mineSweeper.getStatus()[i] = 0;
        }

        //check if there is a saved instance state
        if (param != null) {
            mineSweeper.setNumberOfRows(param.getInt("rows"));
            mineSweeper.setNumberOfColumns(param.getInt("columns"));
            mineSweeper.setData(param.getIntArray("data"));
            mineSweeper.setStatus(param.getIntArray("status"));
            totalSavedSeconds = param.getInt("time");
            boolean noinfo = param.getBoolean("empty");
            mineSweeper.setGameEnded(param.getBoolean("gameended"));
            if (noinfo) {
                savecheck = false;

            } else {
                savecheck = true;
                savedinstancestate = true;
            }
            if (mineSweeper.getNumberOfRows() < mineSweeper.getNumberOfColumns()) {
                int save = mineSweeper.getNumberOfColumns();
                mineSweeper.setNumberOfColumns(mineSweeper.getNumberOfRows());
                mineSweeper.setNumberOfRows(save);
            }
        }

        //parce the Content and Status String if this is loading a saved game
        if (savecheck) {
            if (!savedinstancestate) {
                String[] parcedContent = savedContent.split("");
                String[] parcedStatus = savedStatus.split("");

                StringBuilder line = new StringBuilder();
                for (int i = 0; i < mineSweeper.getNumberOfCells(); i++) {
                    line.append(parcedContent[i + 1]);
                    mineSweeper.getData()[i] = Integer.parseInt(parcedContent[i + 1]);
                    mineSweeper.getStatus()[i] = Integer.parseInt(parcedStatus[i + 1]);
                }
            }
            //flip the info if we are in landscape mode
            if(landscape){
                mineSweeper.setLandscape_data(new int[mineSweeper.getData().length]);
                int x = 1;
                int start = mineSweeper.getNumberOfCells() - mineSweeper.getNumberOfColumns();
                int now = start;
                for (int i = 0; i < mineSweeper.getData().length; i++) {
                    mineSweeper.getLandscape_data()[i] = mineSweeper.getData()[now];
                    now = now - mineSweeper.getNumberOfColumns();
                    if(now < 0) {
                        now = start + x;
                        x++;
                    }
                }
                mineSweeper.setData(mineSweeper.getLandscape_data());

                x = 1;
                start = mineSweeper.getNumberOfCells() - mineSweeper.getNumberOfColumns();
                mineSweeper.setLandscape_status(new int[mineSweeper.getStatus().length]);
                now = start;
                for (int i = 0; i < mineSweeper.getStatus().length; i++) {
                    mineSweeper.getLandscape_status()[i] = mineSweeper.getStatus()[now];
                    now = now - mineSweeper.getNumberOfColumns();
                    if(now < 0) {
                        now = start + x;
                        x++;
                    }
                }
                mineSweeper.setStatus(mineSweeper.getLandscape_status());
            }
        }

        // set up the RecyclerView
        final View heightTest = findViewById(R.id.height_test);
        recyclerView = (RecyclerView) findViewById(R.id.playingfield);

        //uses heightTest to measure the height of the usable screen when it is first drawn
        heightTest.setVisibility(View.VISIBLE);
        firstTime = true;

        //after the first drawing we use the measured height to calculate the maximum height of every cell in the grid
        heightTest.post(new Runnable() {
            @Override
            public void run() {
                //subtract the height of the play_sidebar
                int height;
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    height = recyclerView.getHeight();
                    desired_width = recyclerView.getWidth() - Math.round(25*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
                } else {
                    height = recyclerView.getHeight()- Math.round(32*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
                    desired_width = recyclerView.getWidth();
                }
                //set height of recyclerView so it does not overlap the play_sidebar
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = height;
                params.width = desired_width;
                maxHeight = height/ mineSweeper.getNumberOfRows();
                //cells have a buffer of 2dp, so substract 1dp*2 transformed into pixel value
                maxHeight = maxHeight - Math.round(2*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));

                if (firstTime) {
                    firstTime=false;
                    createAdapter(maxHeight);
                    //after heightTest is made invisible the grid is redrawn, this time with the correct maxheight
                    heightTest.setVisibility(View.GONE);
                }

                recyclerView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(recyclerView.findViewHolderForAdapterPosition(0)!=null )
                        {
                            //loading saved game
                            if (savecheck) {
                                fillSavedGame(savedContent, savedStatus);
                            }
                        }
                    }
                },50);

            }
        });

        //fistLaunch
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            int save = mineSweeper.getNumberOfColumns();
            mineSweeper.setNumberOfColumns(mineSweeper.getNumberOfRows());
            mineSweeper.setNumberOfRows(save);
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, mineSweeper.getNumberOfColumns(), LinearLayoutManager.VERTICAL, false));

        createAdapter(maxHeight);

        firstClick = true;

        //handling the Button that toggles between revealing cells and marking them as mines
        mineSweeper.setMarking(false);
        final Button button = (Button) findViewById(R.id.toggle);
        button.setTextColor(getResources().getColor(R.color.white));
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (mineSweeper.isMarking()) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Button button2 = (Button) findViewById(R.id.toggle2);
                        button2.callOnClick();
                        button2.setBackground(getDrawable(R.drawable.button_highlighted));
                        button2.setText(getString(R.string.untoggled));
                        button2.setTextColor(getResources().getColor(R.color.white));
                    }
                    button.setBackground(getDrawable(R.drawable.button_highlighted));
                    mineSweeper.setMarking(false);
                    button.setText(getString(R.string.untoggled));
                    button.setTextColor(getResources().getColor(R.color.white));
                } else {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        Button button2 = (Button) findViewById(R.id.toggle2);
                        button2.setText(getString(R.string.toggled));
                        button2.setTextColor(getResources().getColor(R.color.black));
                        button2.setBackground(getDrawable(R.drawable.button_highlighted_clicked));
                    }
                    view.setBackground(getDrawable(R.drawable.button_highlighted_clicked));
                    mineSweeper.setMarking(true);
                    button.setText(getString(R.string.toggled));
                    button.setTextColor(getResources().getColor(R.color.black));

                }
            }
        });
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            final Button button2 = (Button) findViewById(R.id.toggle2);
            button2.setTextColor(getResources().getColor(R.color.white));
            button2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (mineSweeper.isMarking()) {
                        button2.setBackground(getDrawable(R.drawable.button_highlighted));
                        button2.setText(getString(R.string.untoggled));
                        button2.setTextColor(getResources().getColor(R.color.white));
                        mineSweeper.setMarking(false);
                        button.setBackground(getDrawable(R.drawable.button_highlighted));
                        button.setText(getString(R.string.untoggled));
                        button.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        view.setBackground(getDrawable(R.drawable.button_highlighted_clicked));
                        button.setBackground(getDrawable(R.drawable.button_highlighted_clicked));
                        mineSweeper.setMarking(true);
                        button.setText(getString(R.string.toggled));
                        button.setTextColor(getResources().getColor(R.color.black));
                        button2.setText(getString(R.string.toggled));
                        button2.setTextColor(getResources().getColor(R.color.black));

                    }
                }
            });
        }

        mineSweeper.setBombsLeft(mineSweeper.getNumberOfBombs());
        mines = (TextView) toolbar.findViewById(R.id.mines);
        mines.setText(String.valueOf(mineSweeper.getBombsLeft()));

        ImageView mines_pic = (ImageView) toolbar.findViewById(R.id.mines_pic);
        mines_pic.setImageResource(R.drawable.mine);

        handler = new Handler();

        bestTimeReader = new DatabaseBestTimeReader(new PFMSQLiteHelper(getApplicationContext()), this);
        bestTimeReader.execute(mineSweeper.getGame_mode());
        writer = new DatabaseWriter(new PFMSQLiteHelper(getApplicationContext()));
    }

    /**
     * This method creates a new PlayRecyclerViewAdapter with the given parameters and connects it to the RecyclerView with the Playing Field
     * @param maximumHeight the maximum height of the singe Cells of the Playing Field
     */
    private void createAdapter(int maximumHeight) {
        adapter = new PlayRecyclerViewAdapter(this, mineSweeper.getData(), maxHeight);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * This method fills the playing Field with the data from the saved game and alters the PlayingField until it is in the same state as the Saved Game and ready to be continued
     * @param savedContent A String coding the Content of each Cell (if there is a Bomb there and how many neighboring Bombs)
     * @param savedStatus A string coding the status of each Cell (if it is untouched, revealed or marked)
     */
    public void fillSavedGame(String savedContent, String savedStatus){
        //Fill the Playing Field by going through Cell by Cell, filling it with the saved content and setting it to the appropriate status
        for (int i = 0; i < mineSweeper.getNumberOfCells(); i++) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
            CellView cell = (CellView) holder.itemView.findViewById(R.id.cell);

            if (mineSweeper.getStatus()[i] == 1) {
                switch (mineSweeper.getData()[i]) {
                    case 0:
                        cell.setText("");
                        break;
                    case 1:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.darkblue));
                        break;
                    case 2:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.darkgreen));
                        break;
                    case 3:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.red));
                        break;
                    case 4:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.darkblue));
                        break;
                    case 5:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.brown));
                        break;
                    case 6:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.cyan));
                        break;
                    case 7:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.black));
                        break;
                    case 8:
                        cell.setText(String.valueOf(mineSweeper.getData()[i]));
                        cell.setTextColor(getResources().getColor(R.color.black));
                        break;
                }

                cell.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.middleblue, null));

                mineSweeper.setCountDownToWin(mineSweeper.getCountDownToWin() - 1);
            } else if (mineSweeper.getStatus()[i] == 2) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                Drawable img = getDrawable(R.drawable.flagge);
                img.setBounds(0, 0, img.getIntrinsicWidth() * cell.getMeasuredHeight() / img.getIntrinsicHeight(), cell.getMeasuredHeight());
                cell.setCompoundDrawables(img,null,null,null);
                mineSweeper.setBombsLeft(mineSweeper.getBombsLeft() - 1);
                mineSweeper.setCountDownToWin(mineSweeper.getCountDownToWin() - 1);
                mines.setText(String.valueOf(mineSweeper.getBombsLeft()));
            }
        }

        timer = (Chronometer) toolbar.findViewById(R.id.chronometer);
        timer.setBase(SystemClock.elapsedRealtime() - (totalSavedSeconds*1000));

        firstClick = true;

    }

    /**
     * This method overrides the onItemClick of the Playing Field cells.
     * @param view the View Containing the Cell where the event was triggered
     * @param position the position of the Cell that was clicked
     */
    @Override
    public void onItemClick(View view, int position) {
        //on the first click the timer must be started and the PlayingField must be filled
        if (firstClick) {
            if (!savecheck) {
                mineSweeper.fillPlayingField(position);
                firstClick = false;
                mineSweeper.setGameEnded(false);

                timer = (Chronometer) toolbar.findViewById(R.id.chronometer);
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();
            } else {
                firstClick = false;
                timer = (Chronometer) toolbar.findViewById(R.id.chronometer);
                timer.setBase(SystemClock.elapsedRealtime() - (totalSavedSeconds*1000));
                timer.start();
            }
        }

        LinearLayout cellview = (LinearLayout) view;
        CellView cell = (CellView) cellview.getChildAt(0);

        //check if cell is already revealed and has the right amount of mines marked
         if (mineSweeper.getStatus()[position] == 1) {
             ArrayList<Integer> revealed = new ArrayList<>();
             mineSweeper.revealAroundCell(position, true, revealed);
             updateForRevealedCells(revealed);
         } else
        //check if we are in marking mode
        if (mineSweeper.isMarking()) {
            //only if the cell is not revealed
            if (mineSweeper.getStatus()[position] != 1) {
                //check if already marked
                if (mineSweeper.getStatus()[position] == 2) {
                    mineSweeper.setCountDownToWin(mineSweeper.getCountDownToWin() + 1);
                    mineSweeper.getStatus()[position] = 0;
                    cell.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mineSweeper.setBombsLeft(mineSweeper.getBombsLeft() + 1);
                    mines.setText(String.valueOf(mineSweeper.getBombsLeft()));
                } else {
                    mineSweeper.getStatus()[position] = 2;
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    Drawable img = getDrawable(R.drawable.flagge);
                    img.setBounds(0, 0, img.getIntrinsicWidth() * cell.getMeasuredHeight() / img.getIntrinsicHeight(), cell.getMeasuredHeight());
                    cell.setCompoundDrawables(img,null,null,null);

                    mineSweeper.setBombsLeft(mineSweeper.getBombsLeft() - 1);
                    mineSweeper.setCountDownToWin(mineSweeper.getCountDownToWin() - 1);
                    mines.setText(String.valueOf(mineSweeper.getBombsLeft()));
                    victoryCheck();
                }
            }
        }
        //normal revealing of the cell
        else {
            ArrayList<Integer> revealed = new ArrayList<>();
            mineSweeper.revealCell(position, revealed);
            updateForRevealedCells(revealed);
        }
    }

    private void updateForRevealedCells(ArrayList<Integer> revealed) {
        for(int cell : revealed){
            updateForRevealedCell(cell);
        }
    }

    private void updateForRevealedCell(int revealedCell) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(revealedCell);
        final CellView cell = (CellView) holder.itemView.findViewWithTag(maxHeight);

        if(mineSweeper.isGameEnded()) {
            Drawable img = getDrawable(R.drawable.mine_x);
            img.setBounds(0, 0, img.getIntrinsicWidth() * cell.getMeasuredHeight() / img.getIntrinsicHeight(), cell.getMeasuredHeight());
            cell.setCompoundDrawables(img,null,null,null);

            timer.stop();

            long gametimeInMillis = SystemClock.elapsedRealtime() - timer.getBase();
            long gametime = gametimeInMillis / 1000;
            int time = (int) gametime;

            parameter.putBoolean("victory", false);
            parameter.putInt("time", time);
            parameter.putString("gameMode", mineSweeper.getGame_mode());
            parameter.putBoolean("newBestTime", newBestTime);

            lockActivityOrientation();
            final Intent tempI = new Intent(this, VictoryScreen.class);
            tempI.putExtras(parameter);
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    startActivityForResult(tempI, 0);
                }
            }, 200);


            //update general statistics (not for user-defined game mode)
            if(!mineSweeper.getGame_mode().equals("user-defined")){
                //first parameter: game mode
                //second parameter: 1 as one match was played
                //third parameter: 1 if game was won, 0 if game was lost
                //fourth parameter: number of uncovered fields
                //fifth parameter: playing time in seconds (for won games only)
                //sixth parameter: playing time in seconds
                //seventh parameter: actual date and time, here 'lost' to indicate that lost game isn't saved in top times list
                Object[] result_params = {mineSweeper.getGame_mode(), 1, 0, (mineSweeper.getNumberOfCells() - mineSweeper.getCountDownToWin()), 0, time, "lost"};
                writer.execute(result_params);
            }
        } else {
            int cellValue = mineSweeper.getData()[revealedCell];
            switch (cellValue) {
                case 0:
                    cell.setText("");
                    break;
                case 1:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.darkblue));
                    break;
                case 2:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.darkgreen));
                    break;
                case 3:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.red));
                    break;
                case 4:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.darkblue));
                    break;
                case 5:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.brown));
                    break;
                case 6:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.cyan));
                    break;
                case 7:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.black));
                    break;
                case 8:
                    cell.setText(String.valueOf(cellValue));
                    cell.setTextColor(getResources().getColor(R.color.black));
                    break;
            }

            cell.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.middleblue, null));

            victoryCheck();
        }
    }

    /**
     * This method checks if the game is won and
     */
    private void victoryCheck() {
        if(mineSweeper.isGameWon()) {
            mineSweeper.setGameEnded(true);

            long gametimeInMillis = SystemClock.elapsedRealtime() - timer.getBase();
            long gametime = gametimeInMillis / 1000;
            int time = (int) gametime;

            timer.stop();

            if(bestTime > time && !mineSweeper.getGame_mode().equals("user-defined")){
                newBestTime = true;
            }

            parameter.putBoolean("victory", true);
            parameter.putInt("time", time);
            parameter.putString("gameMode", mineSweeper.getGame_mode());
            parameter.putBoolean("newBestTime", newBestTime);

            //start victory screen
            lockActivityOrientation();
            Intent tempI = new Intent(this, VictoryScreen.class);
            tempI.putExtras(parameter);
            startActivityForResult(tempI, 0);

            //update general statistics (not for user-defined game mode)
            if(!mineSweeper.getGame_mode().equals("user-defined")){
                //first parameter: game mode
                //second parameter: 1 as one match was played
                //third parameter: 1 if game was won, 0 if game was lost
                //fourth parameter: number of uncovered fields
                //fifth parameter: playing time in seconds (for won games only)
                //sixth parameter: playing time in seconds
                //seventh parameter: actual date and time
                Object[] result_params = {mineSweeper.getGame_mode(), 1, 1, (mineSweeper.getNumberOfCells() - mineSweeper.getCountDownToWin()), time, time, DateFormat.getDateTimeInstance().format(new Date())};
                writer.execute(result_params);
            }
        }
    }

    /**
     * This method is used to close the PlayActivity when a button on the Victory Screen is pressed
     * @param requestCode the Code for the request, should be 0 if all went well
     * @param resultCode the Code for the result, should be RESULT_OK if nothing broke
     * @param data the Intent of the Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    /**
     * This method is used to save the game if the PlayActivity is exited without winning or losing
     */
    @Override
    public void onStop(){

            //check if the game has not ended
            if (!mineSweeper.isGameEnded()){
                //no saving of user defined mode
                if (mineSweeper.getGame_mode().equals("user-defined")) {
                    //do nothing
                } else {
                    //ready the save Data
                    int time;
                    if (firstClick) {
                        time = totalSavedSeconds;
                    } else {
                        timer.stop();
                        long gametimeInMillis = SystemClock.elapsedRealtime() - timer.getBase();
                        long gametime = gametimeInMillis / 1000;
                        time = (int) gametime;
                    }

                    //if we are in landscape mode we have to change our data back to normal before saving
                    if(landscape){

                        mineSweeper.setLandscape_data(new int[mineSweeper.getData().length]);
                        int x = 1;
                        int start = mineSweeper.getNumberOfColumns();
                        int now = start;
                        for (int i = 0; i < mineSweeper.getData().length; i++) {
                            mineSweeper.getLandscape_data()[i] = mineSweeper.getData()[now - x];
                            now = now + mineSweeper.getNumberOfColumns();
                            if(now > mineSweeper.getNumberOfCells()) {
                                now = start;
                                x++;
                            }
                        }
                        mineSweeper.setData(mineSweeper.getLandscape_data());


                        mineSweeper.setLandscape_status(new int[mineSweeper.getStatus().length]);
                        x = 1;
                        start = mineSweeper.getNumberOfColumns();
                        now = start;
                        for (int i = 0; i < mineSweeper.getStatus().length; i++) {
                            mineSweeper.getLandscape_status()[i] = mineSweeper.getStatus()[now - x];
                            now = now + mineSweeper.getNumberOfColumns();
                            if(now > mineSweeper.getNumberOfCells()) {
                                now = start;
                                x++;
                            }
                        }
                        mineSweeper.setStatus(mineSweeper.getLandscape_status());

                        //switch back
                        int save = mineSweeper.getNumberOfColumns();
                        mineSweeper.setNumberOfColumns(mineSweeper.getNumberOfRows());
                        mineSweeper.setNumberOfRows(save);
                    }

                    //check if we need to save into database or not
                    if(isChangingConfigurations()) {
                    } else {
                        StringBuilder content = new StringBuilder();
                        StringBuilder states = new StringBuilder();
                        for (int i = 0; i < mineSweeper.getData().length; i++) {
                            content.append(mineSweeper.getData()[i]);
                            states.append(mineSweeper.getStatus()[i]);
                        }


                        //Save game
                        //first parameter: game mode
                        //second parameter: game time
                        //third parameter: date
                        //fourth parameter: progress
                        //fifth parameter: string coding the content of the playingfield
                        //sixth parameter: string coding the status of the playingfield
                        DatabaseSavedGameWriter writer = new DatabaseSavedGameWriter(new PFMSQLiteHelper(getApplicationContext()), this);
                        Object[] data = {mineSweeper.getGame_mode(), time, DateFormat.getDateTimeInstance().format(new Date()), (((double) mineSweeper.getNumberOfCells() - mineSweeper.getCountDownToWin())/ mineSweeper.getNumberOfCells()), content, states};
                        writer.execute(data);

                        //notify that game is saved
                        Toast saveGameInfo = Toast.makeText(getApplicationContext(), getResources().getString(R.string.gameSaved), Toast.LENGTH_SHORT);
                        saveGameInfo.show();
                        finish();
                    }
                }
            }

        super.onStop();
    }

    /**
     * This method is used to set the best time for comparison
     */
    public void setBestTime(int bt){
        bestTime = bt;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        if(isChangingConfigurations()) {

            if(landscape){

                mineSweeper.setLandscape_data(new int[mineSweeper.getData().length]);
                int x = 1;
                int start = mineSweeper.getNumberOfColumns();
                int now = start;
                for (int i = 0; i < mineSweeper.getData().length; i++) {
                    mineSweeper.getLandscape_data()[i] = mineSweeper.getData()[now - x];
                    now = now + mineSweeper.getNumberOfColumns();
                    if(now > mineSweeper.getNumberOfCells()) {
                        now = start;
                        x++;
                    }
                }
                mineSweeper.setData(mineSweeper.getLandscape_data());


                mineSweeper.setLandscape_status(new int[mineSweeper.getStatus().length]);
                x = 1;
                start = mineSweeper.getNumberOfColumns();
                now = start;
                for (int i = 0; i < mineSweeper.getStatus().length; i++) {
                    mineSweeper.getLandscape_status()[i] = mineSweeper.getStatus()[now - x];
                    now = now + mineSweeper.getNumberOfColumns();
                    if(now > mineSweeper.getNumberOfCells()) {
                        now = start;
                        x++;
                    }
                }
                mineSweeper.setStatus(mineSweeper.getLandscape_status());

            }
            int time;
            if (firstClick) {
                time = totalSavedSeconds;
            } else {
                timer.stop();
                long gametimeInMillis = SystemClock.elapsedRealtime() - timer.getBase();
                long gametime = gametimeInMillis / 1000;
                time = (int) gametime;
            }

            if (mineSweeper.getNumberOfRows() < mineSweeper.getNumberOfColumns()) {
                int save = mineSweeper.getNumberOfColumns();
                mineSweeper.setNumberOfColumns(mineSweeper.getNumberOfRows());
                mineSweeper.setNumberOfRows(save);
            }
            // Save the current game state
            savedInstanceState.putInt("columns", mineSweeper.getNumberOfColumns());
            savedInstanceState.putInt("rows", mineSweeper.getNumberOfRows());
            savedInstanceState.putIntArray("data", mineSweeper.getData());
            savedInstanceState.putIntArray("status", mineSweeper.getStatus());
            savedInstanceState.putInt("time", time);
            savedInstanceState.putBoolean("firstclick", firstClick);
            savedInstanceState.putBoolean("gameended", mineSweeper.isGameEnded());

            Boolean empty;
            if (firstClick && !savecheck) {
                empty  = true;
            } else {
                empty = false;
            }
            savedInstanceState.putBoolean("empty", empty);
        }

            // Always call the superclass so it can save the view hierarchy state
            super.onSaveInstanceState(savedInstanceState);
    }

    private void lockActivityOrientation() {
        Display display = this.getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        int height;
        int width;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            height = display.getHeight();
            width = display.getWidth();
        } else {
            Point size = new Point();
            display.getSize(size);
            height = size.y;
            width = size.x;
        }
        switch (rotation) {
            case Surface.ROTATION_90:
                if (width > height)
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                else
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case Surface.ROTATION_180:
                if (height > width)
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case Surface.ROTATION_270:
                if (width > height)
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                else
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            default :
                if (height > width)
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
}