package org.secuso.privacyfriendlyminesweeper.minesweeper;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class MineSweeper {
    private String game_mode;

    public String getGame_mode() {
        return game_mode;
    }

    public void setGame_mode(String game_mode) {
        this.game_mode = game_mode;
    }

    private int numberOfRows;

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    private int numberOfColumns;

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    private int numberOfBombs;

    public int getNumberOfBombs() {
        return numberOfBombs;
    }

    public void setNumberOfBombs(int numberOfBombs) {
        this.numberOfBombs = numberOfBombs;
    }

    private int numberOfCells;

    public int getNumberOfCells() {
        return numberOfCells;
    }

    public void setNumberOfCells(int numberOfCells) {
        this.numberOfCells = numberOfCells;
    }

    private boolean marking;

    public boolean isMarking() {
        return marking;
    }

    public void setMarking(boolean marking) {
        this.marking = marking;
    }

    private int[] data;

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    private int[] status;

    public int[] getStatus() {
        return status;
    }

    public void setStatus(int[] status) {
        this.status = status;
    }

    private int bombsLeft;

    public int getBombsLeft() {
        return bombsLeft;
    }

    public void setBombsLeft(int bombsLeft) {
        this.bombsLeft = bombsLeft;
    }

    private int countDownToWin;

    public int getCountDownToWin() {
        return countDownToWin;
    }

    public void setCountDownToWin(int countDownToWin) {
        this.countDownToWin = countDownToWin;
    }

    private boolean gameEnded;

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    private int[] landscape_data;

    public int[] getLandscape_data() {
        return landscape_data;
    }

    public void setLandscape_data(int[] landscape_data) {
        this.landscape_data = landscape_data;
    }

    private int[] landscape_status;

    public int[] getLandscape_status() {
        return landscape_status;
    }

    public void setLandscape_status(int[] landscape_status) {
        this.landscape_status = landscape_status;
    }

    public MineSweeper() {
    }

    /**
     * This method fills the playing Field with data. First it puts the needed amount of bombs in random Cells, then Calculates the Number of Neighboring Bomb for each Cell
     *
     * @param notHere the position of the Cell where the user clicked first. This one can not have a Bomb in it
     */
    public void fillPlayingField(int notHere) {

        //put bombs at random positions
        for (int i = 0; i < numberOfBombs; i++) {
            Random randomGen = new Random();
            int position = randomGen.nextInt(numberOfCells);

            //redo if the first clicked cell would get a bomb
            if (position == notHere) {
                i--;
            }
            //9 equals a bomb
            //redo random position if there is a bomb already
            else if (data[position] == 9) {
                i--;
            }
            //redo if placing a bomb at position would produce a cluster of bombs
            //4 or more horizontally and vertically neighbouring bombs are considered to be a cluster
            //possible arrangements that are prevented:
            //1) XX  2) XX   3) XXXX  4) XXX  5) XXX
            //   XX      XX              X        X
            else if (numberOfNeighbouringBombs(position, position, new ArrayList<Integer>()) >= 3) {
                i--;
            } else {
                data[position] = 9;
            }
        }

        //Fill the playing field with numbers depending on bomb position
        for (int pos = 0; pos < numberOfCells; pos++) {

            if (data[pos] != 9) {
                data[pos] = 0;
                //check if position is in one corner of the Field
                //bottom left
                if (pos == 0) {
                    if (data[pos + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //bottom right
                else if (pos == (numberOfColumns - 1)) {
                    if (data[pos - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //top left
                else if (pos == (numberOfCells - numberOfColumns)) {
                    if (data[pos + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //top right
                else if (pos == numberOfCells - 1) {
                    if (data[pos - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //bottom row
                else if (pos < numberOfColumns) {
                    if (data[pos - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //top row
                else if (pos > numberOfCells - numberOfColumns) {
                    if (data[pos - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //left column
                else if (pos % numberOfColumns == 0) {
                    if (data[pos + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //right column
                else if (pos % numberOfColumns == (numberOfColumns - 1)) {
                    if (data[pos - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
                //the rest (inner cells)
                else {
                    if (data[pos - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos + numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns + 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                    if (data[pos - numberOfColumns - 1] == 9) {
                        data[pos] = data[pos] + 1;
                    }
                }
            }
        }
    }

    /**
     * This method counts the number of horizontally and vertically neighbouring bombs of a cell
     *
     * @param position          position of the cell on the playing field
     * @param rootPosition      position / cell that invokes the method (mustn't considered as neighbour)
     * @param checkedNeighbours all checked neighbours are stored to ensure that they are not counted twice
     * @return number of (recursively) neighbouring bombs of the cell at position
     */
    private int numberOfNeighbouringBombs(int position, int rootPosition, ArrayList<Integer> checkedNeighbours) {
        int counterBombs = 0;

        //increase counter if there is a bomb on the cell at position and store the position
        if (data[position] == 9) {
            checkedNeighbours.add(position);
            counterBombs++;
        }

        //bottom left
        if (position == 0) {
            if ((data[position + 1] == 9) && ((position + 1) != rootPosition) && (!checkedNeighbours.contains(position + 1))) {
                counterBombs += numberOfNeighbouringBombs(position + 1,  position, checkedNeighbours);
            }
            if ((data[position + numberOfColumns] == 9) && ((position + numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position + numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position + numberOfColumns,  position, checkedNeighbours);
            }
        }
        //bottom right
        else if (position == (numberOfColumns - 1)) {
            if ((data[position - 1] == 9) && ((position - 1) != rootPosition) && (!checkedNeighbours.contains(position - 1))) {
                counterBombs += numberOfNeighbouringBombs(position - 1, position, checkedNeighbours);
            }
            if ((data[position + numberOfColumns] == 9) && ((position + numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position + numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position + numberOfColumns, position, checkedNeighbours);
            }
        }
        //top left
        else if (position == (numberOfCells - numberOfColumns)) {
            if ((data[position + 1] == 9) && ((position + 1) != rootPosition) && (!checkedNeighbours.contains(position + 1))) {
                counterBombs += numberOfNeighbouringBombs(position + 1,  position, checkedNeighbours);
            }
            if ((data[position - numberOfColumns] == 9) && ((position - numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position - numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position - numberOfColumns, position, checkedNeighbours);
            }
        }
        //top right
        else if (position == (numberOfCells - 1)) {
            if ((data[position - 1] == 9) && ((position - 1) != rootPosition) && (!checkedNeighbours.contains(position - 1))) {
                counterBombs += numberOfNeighbouringBombs(position - 1, position, checkedNeighbours);
            }
            if ((data[position - numberOfColumns] == 9) && ((position - numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position - numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position - numberOfColumns, position, checkedNeighbours);
            }
        }
        //bottom row
        else if (position < numberOfColumns) {
            if ((data[position - 1] == 9) && ((position - 1) != rootPosition) && (!checkedNeighbours.contains(position - 1))) {
                counterBombs += numberOfNeighbouringBombs(position - 1, position, checkedNeighbours);
            }
            if ((data[position + 1] == 9) && ((position + 1) != rootPosition) && (!checkedNeighbours.contains(position + 1))) {
                counterBombs += numberOfNeighbouringBombs(position + 1, position, checkedNeighbours);
            }
            if ((data[position + numberOfColumns] == 9) && ((position + numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position + numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position + numberOfColumns, position, checkedNeighbours);
            }
        }
        //top row
        else if (position > (numberOfCells - numberOfColumns)) {
            if ((data[position - 1] == 9) && ((position - 1) != rootPosition) && (!checkedNeighbours.contains(position - 1))) {
                counterBombs += numberOfNeighbouringBombs(position - 1, position, checkedNeighbours);
            }
            if ((data[position + 1] == 9) && ((position + 1) != rootPosition) && (!checkedNeighbours.contains(position + 1))) {
                counterBombs += numberOfNeighbouringBombs(position + 1, position, checkedNeighbours);
            }
            if ((data[position - numberOfColumns] == 9) && ((position - numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position - numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position - numberOfColumns, position, checkedNeighbours);
            }
        }
        //left column
        else if ((position % numberOfColumns) == 0) {
            if ((data[position + 1] == 9) && ((position + 1) != rootPosition) && (!checkedNeighbours.contains(position + 1))) {
                counterBombs += numberOfNeighbouringBombs(position + 1, position, checkedNeighbours);
            }
            if ((data[position + numberOfColumns] == 9) && ((position + numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position + numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position + numberOfColumns, position, checkedNeighbours);
            }
            if ((data[position - numberOfColumns] == 9) && ((position - numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position - numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position - numberOfColumns, position, checkedNeighbours);
            }
        }
        //right column
        else if ((position % numberOfColumns) == (numberOfColumns - 1)) {
            if ((data[position - 1] == 9) && ((position - 1) != rootPosition) && (!checkedNeighbours.contains(position - 1))) {
                counterBombs += numberOfNeighbouringBombs(position - 1, position, checkedNeighbours);
            }
            if ((data[position + numberOfColumns] == 9) && ((position + numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position + numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position + numberOfColumns, position, checkedNeighbours);
            }
            if ((data[position - numberOfColumns] == 9) && ((position - numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position - numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position - numberOfColumns, position, checkedNeighbours);
            }
        }
        //inner cells
        else {
            if ((data[position - 1] == 9) && ((position - 1) != rootPosition) && (!checkedNeighbours.contains(position - 1))) {
                counterBombs += numberOfNeighbouringBombs(position - 1, position, checkedNeighbours);
            }
            if ((data[position + 1] == 9) && ((position + 1) != rootPosition) && (!checkedNeighbours.contains(position + 1))) {
                counterBombs += numberOfNeighbouringBombs(position + 1, position, checkedNeighbours);
            }
            if ((data[position + numberOfColumns] == 9) && ((position + numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position + numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position + numberOfColumns, position, checkedNeighbours);
            }
            if ((data[position - numberOfColumns] == 9) && ((position - numberOfColumns) != rootPosition) && (!checkedNeighbours.contains(position - numberOfColumns))) {
                counterBombs += numberOfNeighbouringBombs(position - numberOfColumns, position, checkedNeighbours);
            }
        }
        return counterBombs;
    }

    /**
     * This method has two functions. Firstly it checks if the right amount of Bombs is marked around a revealed and clicked cell.
     * Secondly it reveals every Cell that is not marked next to the given Position.
     * @param position position of the cell on the playing field around witch we want to operate
     * @param revealed if true we check if there is the right Amount of Bombs marked next to the revealed Cell at position,
     *                 if false we reaveal all Cells in a Circle around position
     */
    public void revealAroundCell(int position, boolean revealed, ArrayList<Integer> revealedPositions) {

        //if revealed is true then
        if (revealed) {
            //check if the right amount of mines is tagged
            int taggedCells = 0;

            //check if position is in one corner of the Field
            //bottom left
            if (position == 0) {
                if (status[position + 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
            }
            //bottom right
            else if (position == (numberOfColumns - 1)) {
                if (status[position - 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
            }
            //top left
            else if (position == (numberOfCells - numberOfColumns)) {
                if (status[position + 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
            }
            //top right
            else if (position == numberOfCells - 1) {
                if (status[position - 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
            }
            //bottom row
            else if (position < numberOfColumns) {
                if (status[position + 1] == 2) {
                    taggedCells++;
                }
                if (status[position - 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
            }
            //top row
            else if (position > numberOfCells - numberOfColumns) {
                if (status[position + 1] == 2) {
                    taggedCells++;
                }
                if (status[position - 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
            }
            //left column
            else if (position % numberOfColumns == 0) {
                if (status[position + 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
            }
            //right column
            else if (position % numberOfColumns == (numberOfColumns - 1)) {
                if (status[position - 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
            }
            //the rest (inner cells)
            else {
                if (status[position - 1] == 2) {
                    taggedCells++;
                }
                if (status[position + 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
                if (status[position + numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns - 1] == 2) {
                    taggedCells++;
                }
                if (status[position - numberOfColumns + 1] == 2) {
                    taggedCells++;
                }
            }
            if (taggedCells == data[position]) {
                revealAroundCell(position, false, revealedPositions);
            }
        }
        //revealing around the cell because the cell has 0 mines adjacent (or the right amount of marks)
        else {
            //check position of the cell
            //bottom left
            if (position == 0) {
                revealCell(position + 1, revealedPositions);
                revealCell(position + numberOfColumns, revealedPositions);
                revealCell(position + numberOfColumns + 1, revealedPositions);
            }
            //bottom right
            else if (position == (numberOfColumns - 1)) {
                revealCell(position - 1, revealedPositions);
                revealCell(position + numberOfColumns, revealedPositions);
                revealCell(position + numberOfColumns - 1, revealedPositions);
            }
            //top left
            else if (position == (numberOfCells - numberOfColumns)) {
                revealCell(position + 1, revealedPositions);
                revealCell(position - numberOfColumns, revealedPositions);
                revealCell(position - numberOfColumns + 1, revealedPositions);
            }
            //top right
            else if (position == numberOfCells - 1) {
                revealCell(position - 1, revealedPositions);
                revealCell(position - numberOfColumns, revealedPositions);
                revealCell(position - numberOfColumns - 1, revealedPositions);
            }
            //bottom row
            else if (position < numberOfColumns) {
                revealCell(position + 1, revealedPositions);
                revealCell(position - 1, revealedPositions);
                revealCell(position + numberOfColumns, revealedPositions);
                revealCell(position + numberOfColumns + 1, revealedPositions);
                revealCell(position + numberOfColumns - 1, revealedPositions);
            }
            //top row
            else if (position > numberOfCells - numberOfColumns) {
                revealCell(position + 1, revealedPositions);
                revealCell(position - 1, revealedPositions);
                revealCell(position - numberOfColumns, revealedPositions);
                revealCell(position - numberOfColumns + 1, revealedPositions);
                revealCell(position - numberOfColumns - 1, revealedPositions);
            }
            //left column
            else if (position % numberOfColumns == 0) {
                revealCell(position + 1, revealedPositions);
                revealCell(position + numberOfColumns, revealedPositions);
                revealCell(position + numberOfColumns + 1, revealedPositions);
                revealCell(position - numberOfColumns, revealedPositions);
                revealCell(position - numberOfColumns + 1, revealedPositions);
            }
            //right column
            else if (position % numberOfColumns == (numberOfColumns - 1)) {
                revealCell(position - 1, revealedPositions);
                revealCell(position + numberOfColumns, revealedPositions);
                revealCell(position + numberOfColumns - 1, revealedPositions);
                revealCell(position - numberOfColumns, revealedPositions);
                revealCell(position - numberOfColumns - 1, revealedPositions);
            }
            //the rest (inner cells)
            else {
                revealCell(position + 1, revealedPositions);
                revealCell(position - 1, revealedPositions);
                revealCell(position + numberOfColumns, revealedPositions);
                revealCell(position + numberOfColumns + 1, revealedPositions);
                revealCell(position + numberOfColumns - 1, revealedPositions);
                revealCell(position - numberOfColumns, revealedPositions);
                revealCell(position - numberOfColumns + 1, revealedPositions);
                revealCell(position - numberOfColumns - 1, revealedPositions);
            }
        }
    }

    /**
     * This method handles the revealing of a Cell at a specific position
     * @param position position of the cell on the playing field
     */
    public void revealCell(int position, ArrayList<Integer> revealedPositions) {
        //if another cell reveal already lost the game this Method doesnt do anything
        if (gameEnded) {
            return;
        }

        //only reveal if the cell is not marked or revealed
        if (status[position] == 0) {
            revealedPositions.add(position);
            //check for gameloss
            if (data[position] == 9) {
                Log.i("test", "mine at " + position);
                gameEnded = true;
            } else {
                //set cell to revealed
                status[position] = 1;
                countDownToWin--;

                //check if automatic reveal of surrounding cells is needed
                if (data[position] == 0) {
                    revealAroundCell(position, false, revealedPositions);
                }
            }
        }
    }

    public boolean isGameWon() {
        //if all Cells are revealed or marked and the right Number of Bombs is marked
        return countDownToWin == 0 && bombsLeft == 0;
    }
}