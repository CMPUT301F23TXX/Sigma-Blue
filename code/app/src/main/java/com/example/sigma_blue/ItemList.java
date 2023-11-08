package com.example.sigma_blue;

import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ItemList implements IAdaptable<Item> {
    /* Attributes */
    private List<Item> items;
    private ItemDB dbHandler;
    private ItemListAdapter adapter;
    private Account account;

    /* Factory construction */

    public static ItemList newInstance(Account a, ItemDB dbH,
                                       ItemListAdapter adapt) {
        ItemList ret = new ItemList(new ArrayList<>(), a);
        ret.setAdapter(adapt);
        ret.setDatabaseHandler(dbH);
        ret.startListening();
        return ret;
    }

    /**
     * Factory creation for when there isn't an input ArrayList of songs ready for input.
     * @return an instance of the ItemList class holding no item.
     */
    public static ItemList newInstance(Account a) {
        ItemList ret = new ItemList(new ArrayList<Item>(), a);
        ret.setAdapter(ItemListAdapter.newInstance(ret.getList()));
        ret.setDatabaseHandler(ItemDB.newInstance(a));
        ret.startListening();
        return ret;
    }

    /**
     * Factory creation that will bind the ItemList to an ArrayList of Item object that has been
     * pre-initialized.
     * @param items is an ArrayList of the Item object that has been pre-initialized.
     * @return an instance of the ItemList object containing the Item objects that were present in
     * items.
     */
    public static ItemList newInstance(Account a, ArrayList<Item> items) {
        ItemList ret = new ItemList(items, a);
        ret.setAdapter(ItemListAdapter.newInstance(ret.getList()));
        ret.setDatabaseHandler(ItemDB.newInstance(a));
        ret.startListening();
        return ret;
    }

    /**
     * Class constructor. Designed to take in the ArrayList of items for better testing.
     * @param items is an ArrayList of Item objects that the ItemList will hold.
     */
    public ItemList(ArrayList<Item> items, Account account) {
        this.items = items;
    }

    /* Adapter interface methods */

    @Override
    public int getCount() {
        return items.size();
    }

    /**
     * The getItem method returns the Item object (casted to Object) that is stored in the array
     * list at the position.
     * @param position is the index that the item is being retrieved from.
     * @return the Item stored at the index as an Object object.
     */
    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemId(int position) {
        return position;
    }

    /**
     * Returns the amount of elements held in the items ArrayList.
     * @return an integer primitive representing the number of element held in
     * the items ArrayList.
     */
    public int size() {
        return items.size();
    }

    /**
     * Gets the sum of all the value of the item in the ItemList.
     * @author Bach
     * @return the sum of the values of the items contained in this instance in
     * an Optional wrapper. Done this way to enforce explicit handling of the
     * case where there is no items in the list.
     */
    public Function<List<Item>, Optional<Float>> sumValues() {
        return lst -> {
            if (lst.isEmpty()) return Optional.empty();
            else return Optional.of(lst.stream().map(Item::getValue)
                    .reduce(0f, Float::sum));
        };
    }

    /* Setters and Getters */

    /**
     * TODO: Handle addition of the exact same object (unique)
     * @param o is the new item being added. If o is null, then it will not be added to the itemList
     */
    public void add(Item o) {
        if (o != null) {
            this.items.add(o);
            this.dbHandler.add(o);
        }
        updateUI();
        Log.v("Added Item", "Added new item");
    }

    /**
     * @param position is the index which is being removed from the list.
     */
    public void remove(final int position) {
        if (position > -1 && position < size()) {
            this.dbHandler.remove(items.get(position));
            this.items.remove(position);
        }
        else ;
        updateUI();
        Log.v("Removed Item", "Removed an item from item list");
    }

    public void updateUI() {
        adapter.notifyDataSetChanged();
        adapter.updateSumView(sumValues().apply(items));
    }

    public void setDatabaseHandler(final ItemDB dbH) {
        this.dbHandler = dbH;
    }

    public void startListening() {
        dbHandler.startListening(this.adapter, items, sumValues());
    }

    public void setAdapter(final ItemListAdapter adapter) {
        this.adapter = adapter;
    }

    /* Database method */
    public void setList(final List<Item> list) {
        this.items = list;
    }

    public List<Item> getList() {
        return this.items;
    }

    public void setSummaryView(TextView summaryView) {
        this.adapter.setSummaryView(summaryView);
    }
}
