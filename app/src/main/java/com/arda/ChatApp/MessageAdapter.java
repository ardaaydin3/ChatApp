package com.arda.ChatApp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.arda.aydin.BuildConfig;
import com.arda.aydin.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>

{

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference storageRef, storageImageRef;


    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        TextView senderMessageText, receiverMessageText, senderDateTime, receiverDateTime, mediaStatus, Date;
        CircleImageView receiverProfileImage;
        ImageView messageSenderPicture, messageReceiverPicture;
        CardView cardView1,cardView2;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            senderDateTime = itemView.findViewById(R.id.sender_date_time);
            receiverDateTime = itemView.findViewById(R.id.receiver_date_time);
            Date = itemView.findViewById(R.id.date);
            mediaStatus = itemView.findViewById(R.id.media_status);
            cardView1 = itemView.findViewById(R.id.card1);
            cardView2 = itemView.findViewById(R.id.card2);

        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        final String MessageSenderID = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(position);

        //getFrom & getType are getters defined in Messages class
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        storageRef = FirebaseStorage.getInstance().getReference("Document Files/");

        storageImageRef = FirebaseStorage.getInstance().getReference("Image Files/");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        UsersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.receiverDateTime.setVisibility(View.GONE);
        holder.senderDateTime.setVisibility(View.GONE);
        holder.mediaStatus.setVisibility(View.GONE);
        holder.cardView1.setVisibility(View.GONE);
        holder.cardView2.setVisibility(View.GONE);


        if(fromMessageType.equals("text"))
        {

            if(fromUserId.equals(MessageSenderID))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.mediaStatus.setVisibility(View.GONE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage());
                holder.cardView1.setVisibility(View.GONE);
                holder.cardView2.setVisibility(View.VISIBLE);

                Day(position,messages,holder);

                holder.receiverDateTime.setVisibility(View.INVISIBLE);
                holder.senderDateTime.setVisibility(View.VISIBLE);
                holder.senderDateTime.setText(messages.getTime());
            }
            else
            {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.cardView1.setVisibility(View.VISIBLE);
                holder.cardView2.setVisibility(View.GONE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage());

                Day(position,messages,holder);

                holder.senderDateTime.setVisibility(View.INVISIBLE);
                holder.receiverDateTime.setVisibility(View.VISIBLE);
                holder.receiverDateTime.setText(messages.getTime());

            }
        }
        else if(fromMessageType.equals("image"))
        {
            Day(position,messages,holder);
            if(fromUserId.equals(MessageSenderID))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.mediaStatus.setVisibility(View.GONE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2F%E2%80%94Pngtree%E2%80%94vector%20send%20message%20icon_4001946.png?alt=media&token=2ce8b19a-1b17-4a0a-98b2-69c56406091c")
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile)
                        .into(holder.messageSenderPicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e)
                            {
                                Picasso.get()
                                        .load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2F%E2%80%94Pngtree%E2%80%94vector%20send%20message%20icon_4001946.png?alt=media&token=2ce8b19a-1b17-4a0a-98b2-69c56406091c")
                                        .into(holder.messageSenderPicture);
                            }
                        });
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2F%E2%80%94Pngtree%E2%80%94vector%20send%20message%20icon_4001946.png?alt=media&token=2ce8b19a-1b17-4a0a-98b2-69c56406091c")
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile)
                        .into(holder.messageReceiverPicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e)
                            {
                                Picasso.get()
                                        .load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2F%E2%80%94Pngtree%E2%80%94vector%20send%20message%20icon_4001946.png?alt=media&token=2ce8b19a-1b17-4a0a-98b2-69c56406091c")
                                        .into(holder.messageReceiverPicture);
                            }
                        });
            }
        }
        else if(fromMessageType.equals("pdf"))
        {
            Day(position,messages,holder);
            if(fromUserId.equals(MessageSenderID))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.mediaStatus.setVisibility(View.GONE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=e8b641d3-b12f-4f16-802a-f8ab6f030dbd")
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile)
                        .into(holder.messageSenderPicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e)
                            {
                                Picasso.get()
                                        .load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=e8b641d3-b12f-4f16-802a-f8ab6f030dbd")
                                        .into(holder.messageSenderPicture);
                            }
                        });

            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=e8b641d3-b12f-4f16-802a-f8ab6f030dbd")
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile)
                        .into(holder.messageReceiverPicture, new Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e)
                            {
                                Picasso.get()
                                        .load("https://firebasestorage.googleapis.com/v0/b/deneme-30c18.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=e8b641d3-b12f-4f16-802a-f8ab6f030dbd")
                                        .into(holder.messageReceiverPicture);
                            }
                        });
            }
        }


        //for sender side
        if(fromUserId.equals(MessageSenderID))
        {
            holder.itemView.setOnClickListener(v -> {

                if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "G??r??nt??le",
                                    "Benim ????in Sil",
                                    "Herkes ????in Sil",
                                    "??ptal"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Se??enekler");

                    builder.setItems(options, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                           if(which == 0)
                           {

                               final File FileFolder =new File(Environment.getExternalStorageDirectory()+ File.separator+"Dosya"+ File.separator+"Dosya Documents" + File.separator+"Sent");

                               if(userMessagesList.get(position).getType().equals("docx"))
                               {
                                   File InputF = new File(FileFolder,userMessagesList.get(position).getMessageID()+".docx");

                                   if(InputF.exists())
                                   {
                                       Intent intent = new Intent();
                                       intent.setAction(Intent.ACTION_VIEW);
                                       //Uri uri = Uri.parse(InputF.getAbsolutePath());
                                       Uri uri = Uri.fromFile(InputF);
                                       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                       {
                                           uri = FileProvider.getUriForFile(holder.itemView.getContext(), BuildConfig.APPLICATION_ID+".provider",InputF);
                                       }
                                       intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                       intent.setDataAndType(uri,"application/docx");

                                       holder.itemView.getContext().startActivity(intent);
                                   }
                                   else
                                   {
                                       Toast.makeText(holder.itemView.getContext(), "Dosyaya eri??ilemedi", Toast.LENGTH_SHORT).show();
                                   }
                               }

                               else if(userMessagesList.get(position).getType().equals("pdf"))
                               {
                                   File InputF = new File(FileFolder,userMessagesList.get(position).getMessageID()+".pdf");

                                   if(InputF.exists())
                                   {
                                       Intent intent = new Intent();
                                       intent.setAction(Intent.ACTION_VIEW);
                                       //Uri uri = Uri.parse(InputF.getAbsolutePath());
                                       Uri uri = Uri.fromFile(InputF);
                                       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                       {
                                           uri = FileProvider.getUriForFile(holder.itemView.getContext(),BuildConfig.APPLICATION_ID+".provider",InputF);
                                       }
                                       intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                       intent.setDataAndType(uri,"application/pdf");

                                       holder.itemView.getContext().startActivity(intent);
                                   }
                                   else
                                   {
                                       Toast.makeText(holder.itemView.getContext(), "Dosyaya eri??elemedi", Toast.LENGTH_SHORT).show();
                                   }
                               }
                           }
                           else if(which == 1)
                            {
                                DeleteSentMessages(position, holder);

                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }

                           else if(which == 2)
                           {
                               DeleteMessageForEveryone(position, holder);

                               Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                               holder.itemView.getContext().startActivity(intent);
                           }
                        }
                    });
                    builder.show();
                }

                else if(userMessagesList.get(position).getType().equals("text"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Benim ????in Sil",
                                    "Herkes ????in Sil",
                                    "??ptal"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Mesaj?? Sil ?");

                    builder.setItems(options, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if(which == 0)
                            {
                                DeleteSentMessages(position, holder);

                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                            else if(which == 1)
                            {
                                DeleteMessageForEveryone(position, holder);

                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }

                        }
                    });
                    builder.show();
                }


                else if(userMessagesList.get(position).getType().equals("image"))
                {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "G??r??nt??le",
                                    "Benim ????in Sil",
                                    "Herkes ????in Sil",
                                    "??ptal",
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Foto??raf Ayarlar??");

                    builder.setItems(options, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            if(which == 0)
                            {

                                final File ImageFolder =new File(Environment.getExternalStorageDirectory()+ File.separator+"Dosya"+ File.separator+"Dosya Images" + File.separator+"Sent");

                                //opening the file which you have sent

                                File InputF = new File(ImageFolder,userMessagesList.get(position).getMessageID()+".jpg");


                                if(InputF.exists())
                                {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    //Uri uri = Uri.parse(InputF.getAbsolutePath());
                                    Uri uri = Uri.fromFile(InputF);
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                    {
                                        uri = FileProvider.getUriForFile(holder.itemView.getContext(),BuildConfig.APPLICATION_ID+".provider",InputF);
                                    }
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            .setDataAndType(uri,"image/*");
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else
                                {
                                    Toast.makeText(holder.itemView.getContext(), "Dosyaya Eri??ilemedi", Toast.LENGTH_SHORT).show();
                                }

                            }
                            else if(which == 1)
                            {
                                DeleteSentMessages(position, holder);
                                holder.messageSenderPicture.setVisibility(View.GONE);
                            }
                            else if(which == 2)
                            {
                                DeleteMessageForEveryone(position, holder);

                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                }

            });
        }


        //for receiver side
        else
        {
            final File ImageFolder =new File(Environment.getExternalStorageDirectory()+ File.separator+"Dosya"+ File.separator+"Dosya Images");
            final File FileFolder =new File(Environment.getExternalStorageDirectory()+ File.separator+"Dosya"+ File.separator+"Dosya Documents");

            File out1 = new File(FileFolder,userMessagesList.get(position).getMessageID()+".pdf");
            File out2 = new File(FileFolder,userMessagesList.get(position).getMessageID()+".docx");
            File out3 = new File(ImageFolder,userMessagesList.get(position).getMessageID()+".jpg");

            if(out1.exists()||out2.exists()||out3.exists())
            {
                holder.mediaStatus.setVisibility(View.VISIBLE);
                holder.mediaStatus.setText("Kaydedildi");
            }
            else if(userMessagesList.get(position).getType().equals("text"))
            {
                holder.mediaStatus.setVisibility(View.GONE);
            }
            else
            {
                holder.mediaStatus.setVisibility(View.VISIBLE);
                holder.mediaStatus.setText("??ndir");
            }
            

            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {


                                        "Dosyayi ??ndir",
                                        "G??r??nt??le",
                                        "Benim i??in Sil",
                                        "??ptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Se??enekler");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0)
                                {


                                  /* OutputStream outputStream;

                                    DownloadManager downloadManager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    String Url = userMessagesList.get(position).getMessage() ;
                                    Uri uri = Uri.parse(Url);
                                    DownloadManager.Request request = new DownloadManager.Request(uri);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


                                    File filePath = Environment.getExternalStorageDirectory();

                                    dir.mkdir();
                                    File file = new File(dir,System.currentTimeMillis()+".pdf");


                                    try
                                    {

                                    outputStream = new FileOutputStream(file);
                                    }
                                    catch(FileNotFoundException e)
                                    {
                                    e.printStackTrace();
                                    }

                                    outputStream.write(Url)

                                    try{
                                    outputStream.flush();
                                    }
                                    catch(IOException e)
                                    {
                                    e.printStackTrace();
                                    }
                                    try{
                                    outputStream.close();
                                    }
                                    catch(IOException e){
                                    e.printStackTrace();
                                    }



                                    + ".pdf");
                                    downloadManager.enqueue(request);

                                    */



                                 /*DownloadManager mgr = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);

                                    String Url = userMessagesList.get(position).getMessage() ;
                                    Uri downloadUri = Uri.parse(Url);

                                    DownloadManager.Request request = new DownloadManager.Request(
                                            downloadUri);

                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                            .setAllowedOverRoaming(false)
                                            .setTitle("Document File")
                                            .setDescription("Downloading...")


                                    mgr.enqueue(request);

                                  */





                                    boolean success = true;
                                    if(!FileFolder.exists())
                                    {
                                        success = FileFolder.mkdirs();
                                    }
                                    if(success)
                                    {
                                        if(userMessagesList.get(position).getType().equals("pdf"))
                                        {
                                            final File outputFile =new File(Environment.getExternalStorageDirectory(),"Dosya/Dosya Documents/"+userMessagesList.get(position).getMessageID()+".pdf");

                                            long ONE_MEGABYTE = 1024*1024*1024;


                                            storageRef.child(userMessagesList.get(position).getMessageID()+".pdf").getBytes(ONE_MEGABYTE)
                                                    .addOnSuccessListener(new OnSuccessListener<byte[]>()
                                                    {
                                                        @Override
                                                        public void onSuccess(byte[] bytes)
                                                        {

                                                            holder.mediaStatus.setVisibility(View.VISIBLE);
                                                            holder.mediaStatus.setText("Kaydedildi");

                                                            Toast.makeText(holder.itemView.getContext(), "Dosya ??ndirildi", Toast.LENGTH_SHORT).show();

                                                            try
                                                            {
                                                                FileOutputStream fos = new FileOutputStream(outputFile);
                                                                fos.write(bytes);
                                                                fos.close();
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener()
                                                    {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e)
                                                        {
                                                            Toast.makeText(holder.itemView.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                        else if(userMessagesList.get(position).getType().equals("docx"))
                                        {

                                            final File outputFile =new File(Environment.getExternalStorageDirectory(),"Dosya/Dosya Documents/"+userMessagesList.get(position).getName()+".docx");

                                            long ONE_MEGABYTE = 1024*1024*1024;

                                            storageRef.child(userMessagesList.get(position).getMessageID()+".docx").getBytes(ONE_MEGABYTE)
                                                    .addOnSuccessListener(new OnSuccessListener<byte[]>()
                                                    {
                                                        @Override
                                                        public void onSuccess(byte[] bytes)
                                                        {
                                                            holder.mediaStatus.setVisibility(View.VISIBLE);
                                                            holder.mediaStatus.setText("??ndiriliyor");

                                                            Toast.makeText(holder.itemView.getContext(), "Dosya ??ndirildi", Toast.LENGTH_SHORT).show();

                                                            try
                                                            {
                                                                FileOutputStream fos = new FileOutputStream(outputFile);
                                                                fos.write(bytes);
                                                                fos.close();
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener()
                                                    {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e)
                                                        {
                                                            Toast.makeText(holder.itemView.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }

                                    else
                                    {
                                        Toast.makeText(holder.itemView.getContext(), "Please grant permission for storage...", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                else if(which == 1)
                                {
                                    if(userMessagesList.get(position).getType().equals("pdf"))
                                    {
                                        File outputFile =new File(FileFolder,userMessagesList.get(position).getMessageID()+".pdf");

                                        if(outputFile.exists())
                                        {
                                            Intent intent = new Intent();
                                            intent.setAction(android.content.Intent.ACTION_VIEW);
                                            //Uri uri = Uri.parse(outputFile.getAbsolutePath());
                                            Uri uri = Uri.fromFile(outputFile);
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            {
                                                uri = FileProvider.getUriForFile(holder.itemView.getContext(),BuildConfig.APPLICATION_ID+".provider",outputFile);
                                            }
                                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    .setDataAndType(uri,"application/pdf");
                                            holder.itemView.getContext().startActivity(intent);
                                        }
                                        else
                                        {
                                            Toast.makeText(holder.itemView.getContext(), "Dosyaya Eri??ilemedi", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else if(userMessagesList.get(position).getType().equals("docx"))
                                    {
                                        File outputFile =new File(FileFolder,userMessagesList.get(position).getMessageID()+".docx");

                                        if(outputFile.exists())
                                        {
                                            Intent intent = new Intent();
                                            intent.setAction(android.content.Intent.ACTION_VIEW);
                                            //Uri uri = Uri.parse(outputFile.getAbsolutePath());
                                            Uri uri = Uri.fromFile(outputFile);
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            {
                                                uri = FileProvider.getUriForFile(holder.itemView.getContext(),BuildConfig.APPLICATION_ID+".provider",outputFile);
                                            }
                                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    .setDataAndType(uri,"application/docx");
                                            holder.itemView.getContext().startActivity(intent);
                                        }
                                        else
                                        {
                                            Toast.makeText(holder.itemView.getContext(), "Dosyaya Eri??ilemedi", Toast.LENGTH_SHORT).show();
                                        }
                                    }


                                }

                                else if(which == 2)
                                {
                                    DeleteReceiveMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Evet",
                                        "Hay??r",
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj?? Sil ?");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(which == 0)
                                {
                                    DeleteReceiveMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }


                    else if(userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "??ndir",
                                        "G??r??nt??le",
                                        "Benim i??in sil",
                                        "iptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj?? Sil ?");

                        builder.setItems(options, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                                if(which == 0)
                                {

                                  /*  Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);

                                    //inside [userMessagesList.get(position).getMessage()] url of image is stored
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                    */




                                  /*  String ImageUri =  userMessagesList.get(position).getMessage();

                                    FirebaseStorage storageReference = FirebaseStorage.getInstance();

                                    StorageReference storageRef = storageReference.getReferenceFromUrl(ImageUri);

                                    final File rootPath =new File(Environment.getExternalStorageDirectory(),"Download");

                                    if(!rootPath.exists())
                                    {
                                        rootPath.mkdirs();
                                    }

                                    final File localFile = new File(rootPath, userMessagesList.get(position).getMessageID() + ".jpg");

                                    storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                                    {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                        {
                                            Log.e("firebase ",";local tem file created  created " +localFile.toString());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.e("firebase ",";local tem file not created  created " +exception.toString());
                                        }
                                    });

                                   */


                                 /* DownloadManager downloadManager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);

                                  Uri uri = Uri.parse(userMessagesList.get(position).getMessage());
                                  DownloadManager.Request request = new DownloadManager.Request(uri);

                                  request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                  request.setDestinationInExternalFilesDir(holder.itemView.getContext(), DIRECTORY_DOWNLOADS, userMessagesList.get(position).getMessageID() + ".jpg");

                                  downloadManager.enqueue(request);

                                  */

                                 boolean success = true;
                                 if(!ImageFolder.exists())
                                 {
                                     success = ImageFolder.mkdirs();
                                 }
                                 if(success)
                                 {
                                     final File outputFile =new File(ImageFolder,userMessagesList.get(position).getMessageID()+".jpg");


                                     storageImageRef.child(userMessagesList.get(position).getMessageID()+".jpg").getFile(outputFile)
                                             .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                                             {
                                                 @Override
                                                 public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                                 {
                                                     //holder.messageReceiverPicture.setImageURI(Uri.fromFile(outputFile));

                                                     holder.mediaStatus.setVisibility(View.VISIBLE);
                                                     holder.mediaStatus.setText("Kaydedildi");

                                                     Toast.makeText(holder.itemView.getContext(), "Dosya ??ndirildi", Toast.LENGTH_SHORT).show();
                                                 }
                                             })
                                             .addOnFailureListener(new OnFailureListener()
                                             {
                                                 @Override
                                                 public void onFailure(@NonNull Exception e)
                                                 {
                                                     Toast.makeText(holder.itemView.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                                 }
                                             });
                                 }
                                 else
                                     {
                                         Toast.makeText(holder.itemView.getContext(), "Please grant permission for storage...", Toast.LENGTH_SHORT).show();
                                     }

                                }


                                else if(which == 1)
                                {

                                    File outputFile =new File(ImageFolder,userMessagesList.get(position).getMessageID()+".jpg");

                                    if(outputFile.exists())
                                    {
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        //Uri uri = Uri.parse(outputFile.getAbsolutePath());
                                        Uri uri = Uri.fromFile(outputFile);
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        {
                                            uri = FileProvider.getUriForFile(holder.itemView.getContext(),BuildConfig.APPLICATION_ID+".provider",outputFile);
                                        }
                                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                .setDataAndType(uri,"image/*");
                                        holder.itemView.getContext().startActivity(intent);
                                    }

                                    else
                                    {
                                        Toast.makeText(holder.itemView.getContext(), "Dosyaya eri??ilemedi", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                else if(which == 2)
                                {
                                    DeleteReceiveMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }

                }
            });

        }


    }

    private void Day(int position, Messages messages, MessageViewHolder holder)
    {

        Calendar calendar = Calendar.getInstance();
        int i=0;
        String NCurrentDate = messages.getDate();
        StringBuilder date = new StringBuilder();
        while(NCurrentDate.charAt(i)!='-')
        {
            date.append(NCurrentDate.charAt(i));
            i++;
        }

        if(position>0)
        {
            if(userMessagesList.get(position).getDate().equalsIgnoreCase(userMessagesList.get(position-1).getDate()))
            {
                holder.Date.setVisibility(View.GONE);
            }
            else {
                if(calendar.get(Calendar.DATE) == Integer.parseInt(date.toString()))
                {
                    holder.Date.setText("Bug??n");
                    holder.Date.setVisibility(View.VISIBLE);
                }

                else if(calendar.get(Calendar.DATE) - (Integer.parseInt(date.toString()))==1)
                {
                    holder.Date.setText("??nceki G??n");
                    holder.Date.setVisibility(View.VISIBLE);
                }
                else {
                    holder.Date.setText(messages.getDate());
                    holder.Date.setVisibility(View.VISIBLE);
                }
            }
        }
        else
        {
            holder.Date.setText(messages.getDate());
            holder.Date.setVisibility(View.VISIBLE);
        }

    }



    @Override
    public int getItemCount()
    {
        if(userMessagesList.size()==0)
            return 0;
        return userMessagesList.size();
    }

    

    private void DeleteSentMessages(final int position, final MessageViewHolder holder)
    {

        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("Messages")
                .child(userMessagesList.get(position).getFrom()) //getting sender id
                .child(userMessagesList.get(position).getTo()) //getting receiver id
                .child(userMessagesList.get(position).getMessageID()) //getting unique message id
                .removeValue().addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(holder.itemView.getContext(), "Silme Ba??ar??l??", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(holder.itemView.getContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void DeleteReceiveMessages(final int position, final MessageViewHolder holder)
    {

        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Silme Ba??ar??l??", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void DeleteMessageForEveryone(final int position, final MessageViewHolder holder)
    {

        final DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        rootReference.child("Messages")
                .child(userMessagesList.get(position).getFrom()) //getting sender id
                .child(userMessagesList.get(position).getTo()) //getting receiver id
                .child(userMessagesList.get(position).getMessageID()) //getting unique message id
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    rootReference.child("Messages")
                            .child(userMessagesList.get(position).getTo()) //getting sender id
                            .child(userMessagesList.get(position).getFrom()) //getting receiver id
                            .child(userMessagesList.get(position).getMessageID()) //getting unique message id
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Silme Ba??ar??l??", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}

