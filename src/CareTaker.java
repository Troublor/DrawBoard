

import java.util.ArrayList;

/**
 * Created by troub on 2017/10/22.
 */
public class CareTaker {
    private ArrayList<Memento> mementos;

    /**
     * 指向最新备忘内容
     */
    private int index;

    CareTaker(){
        mementos = new  ArrayList<Memento>();
        index = -1;
    }

    public Memento getLastMemento() {
        if (index == 0) {
            return null;
        }
        return mementos.get(--index).clone();
    }

    public Memento getNextMemento(){
        if (index >= mementos.size() - 1) {
            return null;
        }else {
            return mementos.get(++index).clone();
        }
    }

    public void addMemento(Memento memento){
        if (index != mementos.size() - 1) {
            for (int i = index + 1; i < mementos.size(); i++) {
                mementos.remove(i);
            }
        }
        mementos.add(memento.clone());
        index++;
    }

    public boolean undoable(){
        return index > 0;
    }

    public boolean redoable(){
        return index < mementos.size() - 1;
    }
}
