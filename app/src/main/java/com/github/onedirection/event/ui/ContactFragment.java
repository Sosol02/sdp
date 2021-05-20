package com.github.onedirection.event.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.onedirection.R;
import com.github.onedirection.utils.Email;

import java.util.List;

public class ContactFragment extends Fragment {

    private ViewModel viewModel;
    private ContactListAdapter contactListAdapter;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.event_creator_contact_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ViewModel.class);
        setupContactAdder(view);
        setupContactList(view);

    }

    private void setupContactAdder(View view) {
        MutableLiveData<List<String>> contacts = viewModel.lateContacts;
        Button addContactButton = view.findViewById(R.id.addContactButton);
        EditText contactText = view.findViewById(R.id.contactInput);

        addContactButton.setOnClickListener(v -> {
            String emailAddress = contactText.getText().toString();
            if (Email.isValidEmailAddress(emailAddress)) {
                if (!contacts.getValue().contains(emailAddress)) {
                    List<String> newContactList = contacts.getValue();
                    newContactList.add(emailAddress);
                    contacts.setValue(newContactList);
                }
                if (contactListAdapter != null) {
                    contactListAdapter.notifyDataSetChanged();
                }
                contactText.getText().clear();
            } else {
                Toast.makeText(requireContext(), R.string.invalid_email, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupContactList(View view) {
        ListView contactListView = view.findViewById(R.id.contact_email_list);
        contactListAdapter = new ContactListAdapter(requireContext(), viewModel.lateContacts);
        contactListView.setAdapter(contactListAdapter);
    }
}