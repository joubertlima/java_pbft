package util;

import java.util.Random;

public class Constants {
	
	public static final int[] quorumPorts= {6961, 6962, 6963, 6964, 6965, 6966, 6967, 6968, 6969, 6970, 6971, 6972, 6973, 6974, 6975, 6976};
	public static final String[] quorumAddresses = {"localhost", "localhost", "localhost", "localhost", "localhost", "localhost", "localhost", "localhost",
			"localhost", "localhost", "localhost", "localhost", "localhost", "localhost", "localhost", "localhost"};
	public static final int clientResponsesServerPort = 6960;
	public static final String clientResponsesServerAddress = "localhost";
	public static final int numOfClients = 1;
	public static final int primaryPort = 6961;
	public static final String primaryAddress = "localhost";
	public static final String primaryCommandType = "1";
	public static final String prePrepareCommandType = "3";
	public static final String prepareCommandType = "4";
	public static final String commitCommandType = "5";
	public static final String clientResponsesCommandType = "2";
	
	public static final String primaryMeshCommandType = "6";
	public static final String prePrepareMeshCommandType = "7";
	public static final String prepareMeshCommandType = "8";
	public static final String commitMeshCommandType = "9";
	public static final String clientResponsesMeshCommandType = "10";
	
	public static final String messageContentFile = "origin_of_species.txt";
	public static final String faultyMessageContentFile = "hello_world.txt";
	public static final int delayBetweenClientMessages = 1000;
	public static final int maxWaits = 10;
	public static final int delayBetweenMeshSteps = 0;
	
	public static final boolean nodeFaulty = true;	
	public static final int maxFaultyNodes = (quorumAddresses.length-1)/3;
	public static final int minCorrectNodes = 2;
	
	public static final String viewLabel = "Dell_pBFT";
	
	public static final int maxNumAttempts = 2;
	
	public static final int meshDimensions = 2;
	
	public static final int randomSeed = 1;
	public static Random random = new Random(randomSeed);
	
}
