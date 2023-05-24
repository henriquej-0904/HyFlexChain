package pt.unl.fct.di.hyflexchain.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class Utils
{
    public static final JsonMapper json = new JsonMapper();

    public static ByteBuffer toBytes(int value)
    {
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
        b.putInt(value);
        return b.rewind();
    }

    public static ByteBuffer toBytes(long value)
    {
        ByteBuffer b = ByteBuffer.allocate(Long.BYTES);
        b.putLong(value);
        return b.rewind();
    }

    public static void logError(Exception e, Logger log)
    {
        StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            log.error(sStackTrace);
    }

    public static Object readObject(byte[] arr) throws ClassNotFoundException {
        try (ByteArrayInputStream inputArr = new ByteArrayInputStream(arr);
                ObjectInputStream as = new ObjectInputStream(inputArr);) {
            return as.readObject();
        } catch (IOException ex) {
            // never thrown
            throw new RuntimeException(ex);
        }
    }

    public static byte[] writeObject(Object obj) {
        try (ByteArrayOutputStream outputArr = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(outputArr);) {
            os.writeObject(obj);
            os.flush();

            return outputArr.toByteArray();
        } catch (IOException ex) {
            // never thrown
            throw new RuntimeException(ex);
        }
    }

    

    public static String printDate(SimpleDateFormat format, Calendar calendar)
    {
        return format.format(calendar.getTime());
    }

    public static Calendar fromDateString(SimpleDateFormat format, String date) throws ParseException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(format.parse(date));
        return calendar;
    }

    public static byte[] fromBase64(String value){
        return Base64.getDecoder().decode(value);
    }

    public static String toBase64(byte[] value){
        return Base64.getEncoder().encodeToString(value);
    }

    private static String digits = "0123456789abcdef";
    
    /**
     * Retorna string hexadecimal a partir de um byte array de certo tamanho
     * 
     * @param data : bytes a coverter
     * @param length : numero de bytes no bloco de dados a serem convertidos.
     * @return  hex : representacaop em hexadecimal dos dados
     */

   public static String toHex(byte[] data, int length)
    {
        StringBuffer	buf = new StringBuffer();
        
        for (int i = 0; i != length; i++)
        {
            int	v = data[i] & 0xff;
            
            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0xf));
        }
        
        return buf.toString();
    }

    public static byte[] fromHex(String data)
    {
        byte[] res = new byte[data.length()/2];
        int f = 0;
        
        for (int i = 0; i != res.length; i++)
        {
            res[i] = (byte) ((digits.indexOf(String.valueOf(data.charAt(f++))) & 0x0f) << 4);
            res[i] = (byte) (res[i] | (digits.indexOf(String.valueOf(data.charAt(f++))) & 0x0f));
        }
        
        return res;
    }


    
    /**
     * Retorna dados passados como byte array numa string hexadecimal
     * 
     * @param data : bytes a serem convertidos
     * @return : representacao hexadecimal dos dados.
     */
    public static String toHex(byte[] data)
    {
        return toHex(data, data.length);
    }
}

