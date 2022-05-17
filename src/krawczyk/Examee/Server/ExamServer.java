package krawczyk.Examee.Server;

import krawczyk.Examee.Exam;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ExamServer {
	private static ServerSocket server;
	private static int port = 1200;
	private Exam exam;

	public ExamServer(Exam exam) {
		this.exam = exam;
	}

	public void runServer() throws IOException, ClassNotFoundException, InterruptedException {
		server = new ServerSocket(port);

		while(true) {
			System.out.println("Waiting for the request");
			Socket s = server.accept();
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			String studentName = (String) ois.readObject();
			System.out.println("Student: " + studentName + " logged in.");

			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			System.out.println("Sending exam to: " + studentName);
			oos.writeObject(exam);
			oos.close();

			ArrayList<Integer> answers = null;
			while(true) {
				try {
					answers = (ArrayList<Integer>) ois.readObject();
				} catch (EOFException e) {
					break;
				}
			}
			System.out.println("Student: " + studentName + " finished exam.");
			ois.close();
			s.close();
			System.out.println("Saving results to file...");
			saveStudentResults(studentName, countPoints(answers));
			break;
		}

		System.out.println("Server down.");
		server.close();
	}

	private void saveStudentResults(String student, int points) throws IOException {
		String filename = exam.getTitle();
		filename += "-RESULTS-";
		LocalDateTime currentData = LocalDateTime.now();
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss-SS");
		String formattedData = currentData.format(myFormatObj);
		filename += formattedData;
		filename += ".dat";
		BufferedWriter resultFile = new BufferedWriter(new FileWriter(filename));
		resultFile.write(student + ": " + points + "/" + exam.getQuestions().size() + "\n");
	}
	private int countPoints(ArrayList<Integer> answers) {
		int correctAnswers = 0;
		for(int i=0; i < exam.getQuestions().size() ; i++) {

			if(exam.getQuestions().get(i).getCorrectAnswer() == answers.get(i)) {
				correctAnswers++;
			}
		}
		return correctAnswers;
	}
}
