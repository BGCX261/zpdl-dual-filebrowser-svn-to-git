package zpdl.studio.api.dcache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public interface DCacheInteface {
    public DCacheData load(DCacheParam p);
    
    public void done(DCacheParam p);
       
    public DCacheData read(BufferedInputStream in) throws IOException;
    
    public boolean write(DCacheData d, BufferedOutputStream out) throws IOException;
}

