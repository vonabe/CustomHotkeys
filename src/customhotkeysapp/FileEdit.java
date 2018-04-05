package customhotkeysapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author veniamin
 */
public class FileEdit {

    private PrintWriter print = null;
    private File file = null;
    private String path = "";

    public FileEdit(String path) {
            this.path = path;
            this.file = new File(this.path);
    }

    public void write(String msg) throws IOException{
        if(this.file.exists()){

            this.print = new PrintWriter(this.file, "UTF-8");
            this.print.write(msg);
            this.print.flush();
            this.print.close();

            this.file.setLastModified(System.currentTimeMillis());
        }else{
            System.out.println("not file");
        }
    }

    public String readFile() throws FileNotFoundException, IOException{
//        final FileReader reader = new FileReader(this.file);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8);
        final BufferedReader bf = new BufferedReader(reader);
        final StringBuilder builder = new StringBuilder();
        while(bf.ready()){
            String line = bf.readLine();
            builder.append(line);
        }
        bf.close();
        reader.close();
        return builder.toString();
    }

}