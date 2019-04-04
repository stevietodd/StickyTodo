package com.github.stevietodd.stickytodo;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

	MyRecyclerViewAdapter adapter;
	List<String> tasks;
	Stack<String> undos;
	NotificationCompat.Builder notificationBuilder;
	NotificationManagerCompat notificationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		tasks = new ArrayList<>();
		undos = new Stack<>();

		// try to read in existing tasks from file
		try {
			FileInputStream fis = getBaseContext().openFileInput("tasks.txt");
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				tasks.add(line);
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());

				alert.setTitle("Title");
				alert.setMessage("Message");

				// Set an EditText view to get user input
				final EditText input = new EditText(view.getContext());
				alert.setView(input);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// add the task and clear undos
						tasks.add(input.getText().toString());
						undos.clear();
						updateNotificationText();
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});

				alert.show();
			}
		});

		createNotificationChannel();
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
				PendingIntent.FLAG_ONE_SHOT);
		notificationBuilder = new NotificationCompat.Builder(this, "StickyTodoChannel")
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle("StickyTodo")
				.setContentText("StickyTodo")
				.setStyle(new NotificationCompat.BigTextStyle()
						.bigText(String.join("\n", tasks)))
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setOngoing(true)
				.setContentIntent(pendingIntent)
				.setOnlyAlertOnce(true);

		notificationManager = NotificationManagerCompat.from(this);
		// notificationId is a unique int for each notification that you must define
		notificationManager.notify(22, notificationBuilder.build());

		// set up the RecyclerView
		RecyclerView recyclerView = findViewById(R.id.myRecyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		adapter = new MyRecyclerViewAdapter(this, tasks);
		adapter.setClickListener(this);
		recyclerView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			// todo: make this do something later
			return true;
		}

		if (id == R.id.action_undo) {
			if (undos.empty()) {
				return true;
			}

			// find the position and the text
			String undo = undos.pop();
			String tokens[] = undo.split(",");

			// add it back to the task list
			tasks.add(Integer.parseInt(tokens[0]), tokens[1]);

			// update the recyclerview
			adapter.notifyDataSetChanged();

			// update the notification
			updateNotificationText();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemDeleteButtonClick(int position, String text) {
		// add data to undo list, just in case
		undos.push(position + "," + text);

		updateNotificationText();
	}

	private void createNotificationChannel() {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel("StickyTodoChannel", "StickyTodoChannel", importance);
			channel.setDescription("StickyTodoChannel");
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			FileOutputStream fos = getBaseContext().openFileOutput("tasks.txt", Context.MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for (String task : tasks) {
				bw.write(task);
				bw.newLine();
			}

			bw.close();
			fos.close();
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}

	protected void updateNotificationText()
	{
		notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
				.bigText(String.join("\n", tasks)));
		notificationManager.notify(22, notificationBuilder.build());
	}
}
