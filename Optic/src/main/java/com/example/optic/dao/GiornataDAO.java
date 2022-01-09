package com.example.optic.dao;

import com.example.optic.bean.GiornataBean;
import com.example.optic.entities.Giornata;
import com.example.optic.entities.Player;
import com.example.optic.entities.Valutazione;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GiornataDAO {
    private static PlayerDAO daoP;
    private static AdminDAO daoA;
    //private static RefereeDAO daoR;

    public GiornataDAO(PlayerDAO daoP){
        this.daoP = daoP;
    }

    public GiornataDAO(AdminDAO daoA){
        this.daoA = daoA;
    }
    /*
    public GiornataDAO(RefreeDAO daoR){
        this.daoR = daoR;
    }
    */

    //recupero la prima giornata disponibile per la prenotazione dei giocatori
    public Giornata getFirstPlay(String admin) throws Exception{
        Statement stmt = null;
        Giornata play = null;
        try{
            stmt = this.daoA.getConnection().createStatement();
            String sql = "SELECT * FROM giornata WHERE Data=(SELECT min(Data) FROM giornata WHERE curdate() < Data AND fk_UsernameA2 =?)";
            PreparedStatement prepStmt = this.daoA.getConnection().prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            prepStmt.setString(1,admin);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.first()){ //giornata trovata
                rs.first();
                //trasformo il formato di data di mysql
                int idPlay = rs.getInt("idGiornata");
                Calendar data = Calendar.getInstance();
                data.setTime(rs.getDate("Data"));
                int nGiocatori = rs.getInt("NumGiocatori");
                String evento = rs.getString("fk_Nome");
                play = new Giornata(idPlay,data,nGiocatori,evento);
                //chiudo result set
                rs.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return play;
    }

    public Giornata getNextPlay(String admin, Calendar cal){
        Statement stmt = null;
        Giornata play = null;
        Date data = null;
        try{
            stmt = this.daoA.getConnection().createStatement();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//creo formato per la data
            data = cal.getTime();//converto il calendar in data
            String sql = "SELECT * FROM giornata WHERE Data=(SELECT min(G.Data) FROM giornata G WHERE ?<G.Data AND G.fk_UsernameA2 =?)";
            PreparedStatement prepStmt = this.daoA.getConnection().prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            prepStmt.setString(1,dateFormat.format(data));//converto la data in string
            prepStmt.setString(2,admin);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.first()){ //giornata trovata
                rs.first();
                //trasformo il formato di data di mysql
                int idPlay = rs.getInt("idGiornata");
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(rs.getDate("Data"));
                int nGiocatori = rs.getInt("NumGiocatori");
                String evento = rs.getString("fk_Nome");
                play = new Giornata(idPlay,dateCalendar,nGiocatori,evento);
                //chiudo result set
                rs.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return play;
    }

    public Giornata getLastPlay(String admin, Calendar cal){
        Statement stmt = null;
        Giornata play = null;
        Date data = null;
        try{
            stmt = this.daoA.getConnection().createStatement();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//creo formato per la data
            data = cal.getTime();//converto il calendar in data
            String sql = "SELECT * FROM giornata WHERE Data=(SELECT max(G.Data) FROM giornata G WHERE ?>G.Data AND G.fk_UsernameA2 =?)";
            PreparedStatement prepStmt =this.daoA.getConnection().prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            prepStmt.setString(1,dateFormat.format(data));//converto la data in string
            prepStmt.setString(2,admin);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.first()){ //giornata trovata
                rs.first();
                //trasformo il formato di data di mysql
                int idPlay = rs.getInt("idGiornata");
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(rs.getDate("Data"));
                int nGiocatori = rs.getInt("NumGiocatori");
                String evento = rs.getString("fk_Nome");
                play = new Giornata(idPlay,dateCalendar,nGiocatori,evento);
                //chiudo result set
                rs.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return play;
    }

    public ArrayList<Player> getPlayersList(int playId){
        ArrayList<Player> list = new ArrayList<Player>();
        Statement stmt = null;
        String nome;
        int stelle;
        try{
            stmt = this.daoA.getConnection().createStatement();
            String sql = "SELECT Username,Valutazione FROM (player JOIN prenotazione ON Username = fk_Username) WHERE fk_idGiornata =?";
            PreparedStatement prepStmt = this.daoA.getConnection().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            prepStmt.setString(1,Integer.toString(playId));
            ResultSet rs = prepStmt.executeQuery();
            if(rs.first()){
                rs.first();
                do{
                    nome = rs.getString("Username");
                    stelle = rs.getInt("Valutazione");
                    Player p = new Player(nome,stelle);
                    list.add(p);
                }while(rs.next());
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public boolean isDateValid(String admin, Calendar cal){
        boolean res = true;
        Statement stmt = null;
        try{
            stmt = this.daoA.getConnection().createStatement();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//creo formato per la data
            Date data = cal.getTime();//converto il calendar in data
            String sql = "SELECT * FROM giornata WHERE Data =? AND fk_UsernameA2 =?";
            PreparedStatement prepStmt = this.daoA.getConnection().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            prepStmt.setString(1, dateFormat.format(data));
            prepStmt.setString(2, admin);
            ResultSet rs = prepStmt.executeQuery();
            if(!rs.first()){
                res = false;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public void insertPlay(String admin, String evento, Calendar cal){
        Statement stmt = null;
        try{
            stmt = this.daoA.getConnection().createStatement();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//creo formato per la data
            Date data = cal.getTime();//converto il calendar in data
            String sql = "INSERT INTO giornata (Data, NumGiocatori, fk_Nome, fk_UsernameA2) VALUES(?,?,?,?)";
            PreparedStatement prepStmt = this.daoA.getConnection().prepareStatement(sql);
            prepStmt.setString(1, dateFormat.format(data));
            prepStmt.setNull(2, Types.NULL);
            prepStmt.setString(3, evento);
            prepStmt.setString(4, admin);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}