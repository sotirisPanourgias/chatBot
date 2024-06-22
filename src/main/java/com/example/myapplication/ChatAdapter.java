package com.example.myapplication;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messageList;

    public ChatAdapter() {
        this.messageList = new ArrayList<Message>();
    }

    // Μέθοδος για προσθήκη νέου μηνύματος στη λίστα
    public void addMessage(Message message) {
        messageList.add(message);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            // Δημιουργία του ViewHolder για τα μηνύματα του χρήστη
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            // Δημιουργία του ViewHolder για τα μηνύματα του μποτ
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.isFromUser()) {
            ((UserMessageViewHolder) holder).bind(message);
        } else {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).isFromUser()) {
            return 0; // Τύπος του μηνύματος του χρήστη
        } else {
            return 1; // Τύπος του μηνύματος του μποτ
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder για τα μηνύματα του χρήστη
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUserMessage;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserMessage = itemView.findViewById(R.id.textViewUserMessage);
        }

        public void bind(Message message) {
            textViewUserMessage.setText(message.getMessage());
        }
    }

    // ViewHolder για τα μηνύματα του μποτ
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewBotMessage;
        private ImageView imageViewBotIcon;

        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBotMessage = itemView.findViewById(R.id.textViewBotMessage);
            imageViewBotIcon = itemView.findViewById(R.id.imageViewBotIcon);
        }

        public void bind(Message message) {
            textViewBotMessage.setText(message.getMessage());
            // Εδώ μπορείτε να ρυθμίσετε το εικονίδιο του μποτ ανάλογα με το μήνυμα ή να το αφήσετε ως έχει
        }
    }

}

