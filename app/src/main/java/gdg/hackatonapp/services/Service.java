package gdg.hackatonapp.services;

import android.content.Context;

import gdg.hackatonapp.entity.User;

public class Service extends Web {

    public Service(Context context) {
        super(context);
    }

    public long login(User obj) throws Exception {
        return super.post("saveUsr", User.class, obj);
    }
}
