import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.Calendar;


public class FileInputOutput_HS {
	// ファイル入出力

	String file_name = "Othello_AI_weight1-4_learned.txt";
	String file_path = "./weight/" + file_name;
	// String file_path = "../../Othello_AI_weight1-4_learned.txt";


	FileInputOutput_HS(){}

	// ファイルコピー
	private void copyFile(String srcPath, String destPath){
		FileChannel srcChannel = null;
		FileChannel destChannel = null;
		try{
			srcChannel = new FileInputStream(srcPath).getChannel();
			destChannel = new FileOutputStream(destPath).getChannel();
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		}catch(IOException e){
			System.out.println(e);
			e.printStackTrace();
		}finally{
			if(srcChannel != null){
				try {
					srcChannel.close();
				} catch (IOException e){
				}
			}
			if(destChannel != null){
				try{
					destChannel.close();
				}catch(IOException e){
				}
			}
			System.out.println("ファイルコピーしました");
		}
	}

	// ファイルパスに日時の追加
	private String addTime_to_file_path(String file_path){
		Calendar time = Calendar.getInstance();
		int month = time.get(Calendar.MONTH) + 1;
		int day = time.get(Calendar.DATE);
		int hour = time.get(Calendar.HOUR_OF_DAY);
		int minute = time.get(Calendar.MINUTE);
		String res = file_path.substring(0, file_path.length() - 4)
				+ "_" + (month < 10 ? "0" : "") + month +  (day < 10 ? "0" : "") + day
				+ "-" + (hour < 10 ? "0" : "") + hour + (minute < 10 ? "0" : "") + minute
				+ ".txt";
		return res;
	}


	// ファイルパスに任意の文字列を追加
	private String addName_to_file_path(String file_path, String add_string){
		if(add_string.isEmpty()) return file_path;
		return file_path.substring(0, file_path.length() - 4) + "_" + add_string + ".txt";
	}

	// ファイルにHMを書き込み
	private void writeHM(BufferedWriter bufw, double[][][] HM){
		try{
			bufw.write(HM.length + " " + HM[0].length + " " + HM[0][0].length);
			bufw.newLine();
			for(int k=0;k<HM.length;k++){
				for(int i=0;i<HM[0].length;i++){
					for(int j=0;j<HM[0][0].length;j++){
						Double val = HM[k][i][j];
						bufw.write(val.toString());
						if(j != HM[0][0].length - 1) bufw.write(" ");
					}
					bufw.newLine();
				}
			}
		}catch(IOException e){
			System.out.println(e);
		}
		return;
	}

	// ファイルからHMを読み込み
	private void readHM(BufferedReader bufr, double[][][] HM){
		try{
			String str = bufr.readLine();
			//System.out.println(str);
			String strArray[] = str.split(" ");
			int hms = Integer.parseInt(strArray[0]);
			int row = Integer.parseInt(strArray[1]);
			int col = Integer.parseInt(strArray[2]);
			if(hms != HM.length || row != HM[0].length || col != HM[0][0].length){
				System.out.println("おそらくHMSが違う read error おそらくhms    hms : " + HM.length);
			}
			for(int k=0;k<HM.length;k++){
				for(int i=0;i<HM[0].length;i++){
					String data = bufr.readLine();
					String dataArray[] = data.split(" ");
					for(int j=0;j<HM[0][0].length;j++){
						double in = Double.parseDouble(dataArray[j]);
						HM[k][i][j] = in;
					}
				}
			}
		}catch(IOException e){
			System.out.println(e);
		}
	}

	// ファイルにFを書き込み
	private void writeF(BufferedWriter bufw, double[] F){
		try{
			bufw.write(F.length + " ");		//空白がなぜか必要。数字書き込めない
			bufw.newLine();
			for(int k=0;k<F.length;k++){
				Double val = F[k];
				bufw.write(val.toString());
				bufw.newLine();
			}
		}catch(IOException e){
			System.out.println(e);
		}
		return;
	}

	// ファイルからFを読み込み
	private void readF(BufferedReader bufr, double[] F){
		try{
			String str = bufr.readLine();
			//System.out.println(str);
			String strArray[] = str.split(" ");
			int hms = Integer.parseInt(strArray[0]);
			if(hms != F.length) System.out.println("おそらくHMSが違う read error おそらくhms    hms : " + F.length);
			for(int k=0;k<F.length;k++){
				String data = bufr.readLine();
				String dataArray[] = data.split(" ");
				double in = Double.parseDouble(dataArray[0]);
				F[k] = in;
			}
		}catch(IOException e){
			System.out.println(e);
		}
		return;
	}

	// HMとFをtxtファイルにOutput
	public void Output(HarmonySearch HS){
		this.Output(HS, "");
		return;
	}

	// HMとFをtxtファイルにOutput
	public void Output(HarmonySearch HS, String filename_add){
		// 日時付ファイルの保存
		String outputfile_path = this.addName_to_file_path(this.file_path, filename_add);
		outputfile_path = this.addTime_to_file_path(outputfile_path);
		System.out.println();
		System.out.println("[ ファイル書き込み ]");
		System.out.println(outputfile_path);

		File file = new File(outputfile_path);
		try{
			file.createNewFile();
			if (file.exists() && file.isFile() && file.canWrite()){
				BufferedWriter bufw = new BufferedWriter(new FileWriter(file));
				this.writeHM(bufw, HS.HM1);
				this.writeHM(bufw, HS.HM2);
				this.writeHM(bufw, HS.HM3);
				this.writeHM(bufw, HS.HM4);
				this.writeF(bufw, HS.F);
				bufw.close();
				System.out.println("ファイルに書き込みました");
			}else{
				System.out.println("ファイルに書き込めません");
			}
		}catch(IOException e){
			System.out.println(e);
		}

		// 共通ファイルへの保存
		if(filename_add.isEmpty()){
			String newfile_path = this.file_path;
			this.copyFile(outputfile_path, newfile_path);
			System.out.println("[ 共通ファイルへの書き込み ]");
			System.out.println(newfile_path);
		}
		return;
	}

	// 2つのファイルを優れたFから取って合わせる
	public void OutputWithCombiningHS(HarmonySearch HS, HarmonySearch HS2){
		for(int k=0;k<HS.hms;k++){
			for(int i=0;i<HS.NewH1.length;i++) for(int j=0;j<HS.NewH1[0].length;j++) HS.NewH1[i][j] = HS2.HM1[i][j];
			for(int i=0;i<HS.NewH2.length;i++) for(int j=0;j<HS.NewH2[0].length;j++) HS.NewH2[i][j] = HS2.HM2[i][j];
			for(int i=0;i<HS.NewH3.length;i++) for(int j=0;j<HS.NewH3[0].length;j++) HS.NewH3[i][j] = HS2.HM3[i][j];
			for(int i=0;i<HS.NewH4.length;i++) for(int j=0;j<HS.NewH4[0].length;j++) HS.NewH4[i][j] = HS2.HM4[i][j];
			for(int i=0;i<HS.NewF.length;i++) HS.NewF[i] = HS2.F[i];
		}
		HS.displayF();
		HS2.displayF();
		HS.MemoryUpdate();
		HS.displayF();
		this.Output(HS);
		return;
	}

	// HMとFをtxtファイルからInput
	public Boolean Input(HarmonySearch HS){
		this.Input(HS, this.file_path);
		return true;
	}

	// HMとFをtxtファイルからInput、txtファイル名を指定して
	public Boolean Input(HarmonySearch HS, String _file){
		String inputfile_path = _file;
		System.out.println();
		System.out.println("[ ファイル読み込み ]");
		System.out.println(inputfile_path);

		File file = new File(inputfile_path);
		try{
			if (file.exists() && file.isFile() && file.canRead()){
				BufferedReader bufr = new BufferedReader(new FileReader(file));
				this.readHM(bufr, HS.HM1);
				this.readHM(bufr, HS.HM2);
				this.readHM(bufr, HS.HM3);
				this.readHM(bufr, HS.HM4);
				this.readF(bufr, HS.F);
				bufr.close();
				System.out.println("ファイルを読み込みました");
				System.out.println();
			}else{
				System.out.println("ファイルが見つからないか開けません");
				System.out.println();
				return false;
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
			return false;
		}catch(IOException e){
			System.out.println(e);
			return false;
		}
		return true;
	}

	// jarファイル用
	public Boolean Input2_jar(HarmonySearch HS){
		String inputfile_path = this.file_name;
		System.out.println();
		System.out.println("[ ファイル読み込み ]");
		System.out.println(inputfile_path);

		File file = new File(inputfile_path);
		try{
			if (file.exists() && file.isFile() && file.canRead()){
				// http://aidiary.hatenablog.com/entry/20070204/1251465574
				BufferedReader bufr = new BufferedReader(
				        new InputStreamReader(getClass().getResourceAsStream("test.txt"))
				        );
				this.readHM(bufr, HS.HM1);
				this.readHM(bufr, HS.HM2);
				this.readHM(bufr, HS.HM3);
				this.readHM(bufr, HS.HM4);
				this.readF(bufr, HS.F);
				bufr.close();
				System.out.println("ファイルを読み込みました");
				System.out.println();
			}else{
				System.out.println("ファイルが見つからないか開けません");
				System.out.println();
				return false;
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
			return false;
		}catch(IOException e){
			System.out.println(e);
			return false;
		}
		return true;
	}
}
