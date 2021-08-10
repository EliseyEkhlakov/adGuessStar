package com.demo.guessstar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;
    private ImageView imageViewStar;
    private String url = "https://www.forbes.ru/rating/403469-40-samyh-uspeshnyh-zvezd-rossii-do-40-let-reyting-forbes";
    private ArrayList<String> urls;
    private ArrayList<String> names;
    private ArrayList<Button> buttons;
    private int numberOfQuestion;
    private int numberOfRightAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        imageViewStar = findViewById(R.id.imageViewStar);
        urls = new ArrayList<>();
        names = new ArrayList<>();
        buttons = new ArrayList<>();
        buttons.add(button0);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        getContent();
        playGame();
    }

    //
    private void playGame() {
        generateQuestion();
        DownLoadImageTask task = new DownLoadImageTask();
        try {
            Bitmap bitmap = task.execute(urls.get(numberOfQuestion)).get();
            if(bitmap != null){
                imageViewStar.setImageBitmap(bitmap);
                for(int i=0;i<buttons.size();i++){
                    if(i==numberOfRightAnswer){
                        buttons.get(i).setText(names.get(numberOfQuestion));
                    }else{
                       int  wrongAnswer = generateWrongAnswer();
                       buttons.get(i).setText(names.get(wrongAnswer));
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //
    private int generateWrongAnswer(){
        return (int) (Math.random() * names.size());
    }

    //
    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberOfRightAnswer = (int) (Math.random() * buttons.size());
    }

    //
    private void getContent() {
        DownLoadContentTask task = new DownLoadContentTask();
        try {
            String content = task.execute(url).get();
            String start = "class=\"common_rating_list";
            String finish = "<!-- Forbes In-Stat counter -->";
            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(content);
            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
            }
            Log.i("NAME_", splitContent);
            Pattern patterImg = Pattern.compile("<img src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\" ");
            Matcher matcherImg = patterImg.matcher(splitContent);
            Matcher matcherName = patternName.matcher(splitContent);
            while (matcherImg.find()) {
                urls.add(matcherImg.group(1));
            }
            while (matcherName.find()) {
                names.add(matcherName.group(1));
            }

            for (String s : names) {
                Log.i("NAME_", s);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onClickAnswer(View view) {
        Button button = (Button) view;
        String tag = button.getTag().toString();
        if(Integer.parseInt(tag) == numberOfRightAnswer){
            Toast.makeText(this, "Правильный ответ", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Неправильный ответ, правильный:" + names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }

    //
    private static class DownLoadContentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader isRreader = new InputStreamReader(in);
                BufferedReader reader = new BufferedReader(isRreader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }
    }

    //
    private static class DownLoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection connection = null;

            try {
                url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }
    }
}