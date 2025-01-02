package com.karthiTech.admin;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class User {
        @PropertyName("Name")
        private String Name;
        @PropertyName("Email")
        private String Email;
        @PropertyName("Phone")
        private String Phone;
        @PropertyName("Password")
        private String Password;
        @PropertyName("isVerified")
        private boolean isVerified;
        @PropertyName("LastLogin")
        private Timestamp loginlogin;
        public User() {
        }

        public User(String name, String email, String phone, String password, boolean isVerified, Timestamp lastLogin) {
            this.Name = name;
            this.Email = email;
            this.Phone = phone;
            this.Password = password;
            this.isVerified = isVerified;
            this.loginlogin = lastLogin;
        }


        @PropertyName("Name")
        public String getName() {
            return Name;
        }

        @PropertyName("Name")
        public void setName(String name) {
            this.Name = name;
        }

        @PropertyName("Email")
        public String getEmail() {
            return Email;
        }

        @PropertyName("Email")
        public void setEmail(String email) {
            this.Email = email;
        }

        @PropertyName("Phone")
        public String getPhone() {
            return Phone;
        }

        @PropertyName("Phone")
        public void setPhone(String phone) {
            this.Phone = phone;
        }

        @PropertyName("Password")
        public String getPassword() {
            return Password;
        }

        @PropertyName("Password")
        public void setPassword(String password) {
            this.Password = password;
        }

        @PropertyName("isVerified")
        public boolean isVerified() {
            return isVerified;
        }

        @PropertyName("isVerified")
        public void setVerified(boolean verified) {
            isVerified = verified;
        }

        @PropertyName("LastLogin")
        public Timestamp getLoginlogin(){
            return loginlogin;
        }

    @PropertyName("LastLogin")
    public void setLoginlogin(Timestamp lastLogin){
            this.loginlogin = lastLogin;
    }

}
