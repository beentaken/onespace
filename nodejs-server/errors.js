function define(name, value) {
    Object.defineProperty(exports, name, {
        value:      value,
        enumerable: true
    });
}


define("DATABASE_ERROR", 
  { "code" : 1001,
    "usermessage" : "Database error",
    "internalmessage" : "Database error",
    "infolink" : "http://.../1001"
  }
);

define("AUTHENTICATION_TOKEN_ERROR", 
  { "code" : 2001,
    "usermessage" : "Failed to authenticate token",
    "internalmessage" : "Failed to authenticate token",
    "infolink" : "http://.../2001"
  }
);

define("NO_TOKEN_PROVIDED_ERROR", 
  { "code" : 2002,
    "usermessage" : "No authentication token provided",
    "internalmessage" : "No authentication token provided",
    "infolink" : "http://.../2002"
  }
);

define("USER_ALREADY_EXISTS", 
  { "code" : 3001,
    "usermessage" : "A user with this email address already exists",
    "internalmessage" : "A user with this email address already exists",
    "infolink" : "http://.../3001"
  }
);

define("USER_NOT_ACTIVATED", 
  { "code" : 3002,
    "usermessage" : "User is not activated",
    "internalmessage" : "User is not activated",
    "infolink" : "http://.../3002"
  }
);

define("USER_ALREADY_ACTIVATED", 
  { "code" : 3003,
    "usermessage" : "User is already activated",
    "internalmessage" : "User is already activated",
    "infolink" : "http://.../3003"
  }
);

define("INCORRECT_ACTIVATION_CODE", 
  { "code" : 3004,
    "usermessage" : "Incorrect activation code",
    "internalmessage" : "Incorrect activation code",
    "infolink" : "http://.../3004"
  }
);

define("INCORRECT_ACCESS_CREDENTIALS", 
  { "code" : 3005,
    "usermessage" : "Incorrect email or password",
    "internalmessage" : "Incorrect email or password",
    "infolink" : "http://.../3005"
  }
);

define("INVALID_PASSWORD", 
  { "code" : 3006,
    "usermessage" : "",
    "internalmessage" : "Invalid password",
    "infolink" : "http://.../3006"
  }
);

define("PASSWORD_HASH_ERROR", 
  { "code" : 3007,
    "usermessage" : "Security error",
    "internalmessage" : "Security error: bcrypt failed",
    "infolink" : "http://.../3007"
  }
);

define("USER_ID_NOT_FOUND", 
  { "code" : 3008,
    "usermessage" : "User ID not found",
    "internalmessage" : "User ID not found",
    "infolink" : "http://.../3008"
  }
);


define("FCN_SEND_FAILED_ERROR", 
  { "code" : 4001,
    "usermessage" : "Could not sent message to FCM",
    "internalmessage" : "Could not sent message to FCM",
    "infolink" : "http://.../4001"
  }
);