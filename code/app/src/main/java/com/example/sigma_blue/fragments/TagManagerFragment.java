package com.example.sigma_blue.fragments;

import com.example.sigma_blue.context.GlobalContext;
import com.example.sigma_blue.databinding.TagManagerFragmentBinding;
import com.example.sigma_blue.R;
import com.example.sigma_blue.entity.tag.Tag;
import com.example.sigma_blue.entity.tag.TagList;
import com.example.sigma_blue.entity.tag.TagListAdapter;
import com.example.sigma_blue.activities.AddEditActivity;
import com.example.sigma_blue.entity.item.Item;
import com.google.common.base.VerifyException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Objects;

public class TagManagerFragment extends Fragment {
    private ArrayList<Tag> tagsData; // Tags that are applied to the Item, if applicable.

    // Shared value between parent calling activity and other fragments
    private GlobalContext globalContext;

    // Globally defined TagList that stores all tags defined for a particular user.
    private TagList tagList;
    // Fragment binding
    private TagManagerFragmentBinding binding;

    // Fragment UI components
    public TagListAdapter tagListAdapter;
    private Button tagCreateButton;
    private Button tagEditButton;
    private Button backButton;
    private Button confirmButton;
    private ListView tagsListView;

    public TagManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Method to inflate layout of the fragment, as well as bind UI components.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = TagManagerFragmentBinding.inflate(inflater, container, false);

        // Bind the UI components
        tagCreateButton = binding.getRoot().findViewById(R.id.tagManageCreateButton);
        tagEditButton = binding.getRoot().findViewById(R.id.tagManageEditButton);
        backButton = binding.getRoot().findViewById(R.id.tagManageBackButton);
        confirmButton = binding.getRoot().findViewById(R.id.tagManageConfirmButton);
        tagsListView = binding.getRoot().findViewById(R.id.tagManagerListView);

        return binding.getRoot();
    }

    /**
     * Method to set the details of the selected Item, if applicable in the fragment,
     * and handle button interactions.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Tags in tagsData are passed from the current item, if applicable.
        // The Account of the user that is signed in is also passed into the fragment.
        final AddEditActivity activity = (AddEditActivity) requireActivity();

        // Load the shared data
        globalContext = GlobalContext.getInstance();
        final Item currentItem = globalContext.getCurrentItem();

        tagsData = (ArrayList<Tag>) globalContext.getTagList().getTags();

        if (globalContext.getCurrentState().equals("add_tags_to_item")) {
            // User is opening the tag manager fragment on an existing fragment.
            // Check tags already applied onto the item.
            for (Tag t: currentItem.getTags()) {
                t.setChecked(true);
            }

        } else if (globalContext.getCurrentState().equals("tag_multi_select")){
            // The user is applying a selection of tags to multiple Items, we just want to
            // return an ArrayList of Tags that we can apply.
            // Here we simply get the globally defined tags.
        } else {
            throw new VerifyException("bad state");
        }


        // Here we should obtain the union of the TagList's Tags with tagsData,
        // but for now I will just join the two
        // TODO Consider the checked status, we probably should get unique tags without regard
        //  to the isChecked status.

        /* Link the adapter to the UI */
        tagListAdapter = TagListAdapter.newInstance(tagsData, getContext());
        tagsListView.setAdapter(tagListAdapter);
        updateTagListView();

        tagList.setAdapter(tagListAdapter);

        /* On click listeners */

        // Handle the checkbox, and the checked state for the user selecting an item
        tagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tagsData.get(position).toggleChecked();
                updateTagListView();
            }
        });

        // Direct user to go to the tag add fragment
        tagCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(TagManagerFragment.this).navigate(R.id.action_tagManagerFragment_to_tagAddFragment);
            }
        });

        // Direct user to go to the tag edit fragment
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(TagManagerFragment.this).navigate(R.id.action_tagManagerFragment_to_editFragment);
            }
        });

        // Handle the user confirming the tag addition.
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTagListView();
                ArrayList<Tag> tagsConfirmed = new ArrayList<>();
                for (Tag t: tagsData) {
                    if (t.isChecked()) {
                        tagsConfirmed.add(t);
                    }
                }

                //Bundle bundle = new Bundle();
                //bundle.putSerializable(EditFragment.ARG_TAGS, tagsConfirmed);
                //NavHostFragment.findNavController(TagManagerFragment.this).navigate(R.id.action_tagManagerFragment_to_editFragment, bundle);
                if (Objects.equals(globalContext.getCurrentState(), "tag_multi_select"))
                {
                    currentItem.setTags(tagsConfirmed);
                    globalContext.newState("view_list_activity");
                    activity.returnAndClose();
                }
                else
                {
                    currentItem.setTags(tagsConfirmed);
                    globalContext.newState("edit_fragment");
                    NavHostFragment.findNavController(TagManagerFragment.this).navigate(R.id.action_tagManagerFragment_to_editFragment);
                }

            }
        });

    }

    /**
     * Method called when the fragment is no longer in use; unbinds all UI elements.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Updates the adapter, as well as the Edit Tag button when called. The user should
     * not be able to edit a tag if they have selected 0, or than 1 tags.
     */
    public void updateTagListView() {
        tagListAdapter.notifyDataSetChanged();

        // Poll through each of the tags and check if they are checked
        int checkedTags = 0;
        for (Tag t: tagsData) {
            if (t.isChecked()) { checkedTags++; }
        }

        if (checkedTags == 1) {
            tagEditButton.setEnabled(true);
        } else {
            tagEditButton.setEnabled(false);
        }
    }
}