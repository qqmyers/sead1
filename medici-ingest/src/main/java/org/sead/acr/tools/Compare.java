package org.sead.acr.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Jim
 * 
 */
public class Compare {

	
	static final String loc1 = "/project/nced/repository/Repository";
	static final String loc2 = "/repository/data";
	static final String loc3 = "/project/nced/repository/Repository/ecogeomorphology";
	static final String loc4 = "/project/nced/repository/Repository/long_term_dynamics/data";
	static final String loc5 = "/project/nced/repository/Repository/other_datasets/legacy_data";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		HashMap<String, Long> backlog = new HashMap<String, Long>();
		HashMap<String, Long> forelog = new HashMap<String, Long>();
		Path inputFilename = FileSystems.getDefault().getPath(
				"c:/users/Jim/workspace/Compare", args[0]);
		BufferedReader br;
		Path seadInputFilename = FileSystems.getDefault().getPath(
				"c:/users/Jim/workspace/Compare", args[1]);
		BufferedReader br2;

		Path outputFilename = FileSystems.getDefault().getPath(
				"c:/users/Jim/workspace/Compare", "compare.csv");
		try {
			PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
					outputFilename, Charset.forName("ISO_8859_1"),
					new OpenOption[] { StandardOpenOption.CREATE,
							StandardOpenOption.WRITE }));

			br = Files.newBufferedReader(inputFilename,
					Charset.forName("ISO_8859_1"));
			br2 = Files.newBufferedReader(seadInputFilename,
					Charset.forName("ISO_8859_1"));

			long dirLineNum = 0;
			long seadLineNum = 0l;
			long matches = 0l;

			long matchSize = 0;
			while (br.ready() ) { //&&(dirLineNum<1000)
				dirLineNum++;

				//System.out.println(dirLineNum);
				KeyVal kv = readDirEntry(br);
				if (kv != null) {

					String curName = kv.getName();
					//Partial Order is important
					if(curName.startsWith(loc3)) {
						curName = curName.substring(loc3.length());
					} else	if(curName.startsWith(loc5)) {
						curName = curName.substring(loc5.length());
					} else	if(curName.startsWith(loc4)) {
						curName = curName.substring(loc4.length());
					}else	if(curName.startsWith(loc1)) {
						curName = curName.substring(loc1.length());
					} else if(curName.startsWith(loc2)) {
						curName = curName.substring(loc2.length());
					} 
					//System.out.println(curName);
					for (String key : backlog.keySet()) {
						//System.out.println("key: " + key);
						
						if (curName.equals(key)) {
							long seadSize = backlog.get(key);
							//System.out.println(dirLineNum + ": backlog match on name");

							if (kv.getSize() == seadSize) {
								matches++;
								matchSize += seadSize;
								backlog.remove(key);
								kv = null;
								break;
							} else {
								pw.println("\"" + curName + "\",\"" + key + "\","
										+ kv.getSize() + "," + seadSize + ", SIZE MISMATCH (BL)");
								backlog.remove(key);
								kv = null;
								break;
							}
						}
					}
					while (br2.ready() && (kv != null)) {
						seadLineNum++;
						
						
						System.out.println(dirLineNum + " : " + seadLineNum + " : " + matches);
						//System.out.println(curName);
						KeyVal seadKV = readSeadEntry(br2);
						if (seadKV != null) {
							//System.out.println("new key: " + seadKV.getName());
							long seadSize = seadKV.getSize();
							String seadName=seadKV.getName();
							seadName = seadName.replaceAll("%3A",":");
							
							if (curName.equals(seadName)) {
								//System.out.println(dirLineNum + ": new match on name");

								if (kv.getSize() == seadSize) {
									matches++;
									matchSize += seadSize;
									kv = null;
								} else {
									pw.println("\"" + curName + "\",\"" + seadName
											+ "\"," + kv.getSize() + ","
											+ seadSize + ", SIZE MISMATCH");
									kv = null;
								}
							} else {
								//System.out.println("adding: "+ seadKV.getName());

								backlog.put(seadName, seadKV.getSize());
							}

							if (kv != null) {
								if (kv.getSize() < seadSize) {
									// Read all at same size - report mismatch
									// or size diff
									//String temp = "Now: " + curName + "," + dirLineNum + " : " + seadLineNum + " : " + matches;
									//System.out.println(temp);
									//pw.println(temp);
									//pw.flush();
									forelog.put(curName, kv.getSize());
									/*															
									pw.println("\"" + curName
											+ "\",,"
											+ kv.getSize()
											+ ",, NOT FOUND IN SEAD OR SIZE CHANGE");
*/											
											
									kv = null;
								}
							}
						}
					}
					// else read new lines until match
					// if match, account else add to HM
				}
			}

			pw.println("MatchCount: " + matches);
			pw.println("MatchSize: " + matchSize);
			for (String key : backlog.keySet()) {
				boolean found = false;
				@SuppressWarnings("unchecked")
				Set<String> forelogKeys = ((HashMap<String, Long>) forelog.clone()).keySet();

				for (String dirKey : forelogKeys) {
					if(!found) {
					if(dirKey.equals(key)) {
						pw.println("\"" + dirKey
								+ "\",\"" + key + "\","
								+ forelog.get(dirKey)
								+ "," + backlog.get(key) + ",SIZE CHANGE");
						forelog.remove(dirKey);
						found=true;
					}
					}
				}
				if(!found) {	
				
				pw.println(",\"" + key + "\",," + backlog.get(key) + ", NOT FOUND IN DIR");
				}
			}
			for(String key: forelog.keySet()) {
				pw.println("\"" + key
						+ "\",,"
						+ forelog.get(key)
						+ ",, NOT FOUND IN SEAD");
			}

			pw.println("Dir Lines Read: " + dirLineNum);
			pw.println("Sead Entries Read: " + seadLineNum);
			pw.flush();
			pw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static KeyVal readDirEntry(BufferedReader br) throws IOException {
		KeyVal kv = new KeyVal();

		String nextLine = br.readLine();

		if (!nextLine.startsWith("---")) {
			if (nextLine.startsWith("\"")) {
				String[] preCells = nextLine.split("\"", 3);
				// pw.println(preCells[1] + ":::" + preCells[2]);
				kv.setName(preCells[1]);
				String[] cells = preCells[2].split(",");
				kv.setSize(Long.parseLong(cells[1]));

			} else {
				String[] cells = nextLine.split(",");
				// pw.println(lineNum + ": " + cells[0]);
				kv.setName(cells[0]);
				kv.setSize(Long.parseLong(cells[1]));
			}
			return kv;
		}
		return null;
	}

	private static KeyVal readSeadEntry(BufferedReader br2) throws IOException {
		KeyVal seadKV = new KeyVal();
		String nextLine = br2.readLine();
		if (!nextLine.startsWith("---")) {
			if (nextLine.startsWith("\"")) {
				String[] preCells = nextLine.split("\"");
				// pw.println(preCells[0]);
				// pw.println(preCells[1] + ":::" + preCells[2]);
				if (preCells.length == 3) {
					String[] cells = preCells[2].split(",");
					seadKV.setName(cells[1]);
					seadKV.setSize(Long.parseLong(cells[2]));
				} else {
					// pw.println(preCells.length);
					// pw.println(preCells[3] + ":::" + preCells[4]);
					String[] cells = preCells[4].split(",");
					seadKV.setName(preCells[3]);
					seadKV.setSize(Long.parseLong(cells[1]));

				}

			} else {
				String[] cells = nextLine.split(",");
				// pw.println(lineNum + ": " + cells[0]);
				seadKV.setSize(Long.parseLong(cells[1]));
			}
			return seadKV;
		}
		return null;
	}
}
