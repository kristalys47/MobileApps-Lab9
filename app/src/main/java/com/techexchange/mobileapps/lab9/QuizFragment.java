package com.techexchange.mobileapps.lab9;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class QuizFragment extends Fragment {
    private TextView questionText;
    private TextView correctText;
    private Button leftButton;
    private Button rightButton;
    private Button nextButton;
    static final String KEY_SCORE = "Score";

    private int currentScore = 0;

    private List<Question> questionList;
    private int currentQuestionIndex;
    String TAG = MainActivity.class.getSimpleName();


    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FirebaseApp app = FirebaseApp.initializeApp(getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance(app);
        database.goOnline();

        DatabaseReference ref = database.getReference();
        ref = ref.child("lab9").child("questions");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onQuestionListChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "The database transaction was cancelled",
                        databaseError.toException());
            }

            View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);

        });

        questionList = new ArrayList<Question>();

        View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        questionText = rootView.findViewById(R.id.question_text);
        leftButton = rootView.findViewById(R.id.left_button);
        leftButton.setOnClickListener(this::onAnswerButtonPressed);
        rightButton = rootView.findViewById(R.id.right_button);
        rightButton.setOnClickListener(this::onAnswerButtonPressed);

        nextButton = rootView.findViewById(R.id.next_button);
        nextButton.setEnabled(false);
        nextButton.setOnClickListener(this::onNextButtonPressed);

        correctText = rootView.findViewById(R.id.correct_incorrect_text);
        currentQuestionIndex = 0;
        if (savedInstanceState != null) {
            currentQuestionIndex = savedInstanceState.getInt("CurrentIndex");
            currentScore = savedInstanceState.getInt("CurrentScore");
        }
        updateView();

        return rootView;
    }

    private void onQuestionListChanged(DataSnapshot dataSnapshot) {
        List<Question> qList = new ArrayList<>();
        for (DataSnapshot q: dataSnapshot.getChildren()){
            questionList.add(new Question(q.child("question").getValue().toString(), q.child("correctAnswer").getValue().toString(), q.child("wrongAnswer").getValue().toString()));
        }
        updateView();
    }


    private void onNextButtonPressed(View v) {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            updateView();
        } else {
            Intent scoreIntent = new Intent(getActivity(), ScoreActivity.class);
            scoreIntent.putExtra(KEY_SCORE, currentScore);
            startActivityForResult(scoreIntent, 0);
        }
        leftButton.setEnabled(true);
        rightButton.setEnabled(true);
        nextButton.setEnabled(false);
    }

    private void onAnswerButtonPressed(View v) {
        Button selectedButton = (Button) v;
        Question ques = questionList.get(currentQuestionIndex);
        if (ques.getCorrectAnswer().contentEquals(selectedButton.getText())) {
            correctText.setText("Correct!");
            currentScore++;
        } else {
            correctText.setText("Wrong!");
        }
        leftButton.setEnabled(false);
        rightButton.setEnabled(false);
        nextButton.setEnabled(true);
    }

    private List<Question> initQuestionList() {
        Resources res = getResources();
        String[] questions = res.getStringArray(R.array.questions);
        String[] correctAnswers = res.getStringArray(R.array.correct_answers);
        String[] wrongAnswers = res.getStringArray(R.array.incorrect_answers);

        // Make sure that all arrays have the same length.

        List<Question> qList = new ArrayList<>();
        for (int i = 0; i < questions.length; ++i) {
            qList.add(new Question(questions[i], correctAnswers[i], wrongAnswers[i]));
        }
        return qList;
    }


    private void updateView() {
        if(questionList.size() <= 0){
            questionText.setVisibility(View.INVISIBLE);
            leftButton.setVisibility(View.INVISIBLE);
            rightButton.setVisibility(View.INVISIBLE);
            nextButton.setVisibility(View.INVISIBLE);
            correctText.setText("There are no questions in the database.");

        }
        else {
            questionText.setVisibility(View.VISIBLE);
            leftButton.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            Question currentQuestion = questionList.get(currentQuestionIndex);
            questionText.setText(currentQuestion.getQuestion());
            if (Math.random() < 0.5) {
                leftButton.setText(currentQuestion.getCorrectAnswer());
                rightButton.setText(currentQuestion.getWrongAnswer());
            } else {
                rightButton.setText(currentQuestion.getCorrectAnswer());
                leftButton.setText(currentQuestion.getWrongAnswer());
            }
            nextButton.setEnabled(false);
            correctText.setText(R.string.initial_correct_incorrect);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CurrentIndex", currentQuestionIndex);
        outState.putInt("CurrentScore", currentScore);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean repeat = data.getBooleanExtra(ScoreActivity.KEY_RESTART_QUIZ, true);
        if (resultCode != Activity.RESULT_OK || requestCode != 0 || data == null) {
            getActivity().finish();
        } else if (repeat) {
            currentQuestionIndex = 0;
            currentScore = 0;
        } else
            getActivity().finish();
    }


}
