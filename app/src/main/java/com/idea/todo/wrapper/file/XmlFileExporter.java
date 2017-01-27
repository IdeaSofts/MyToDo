package com.idea.todo.wrapper.file;

import android.os.Handler;

import com.idea.todo.db.Database;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.ToDoInfo;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class XmlFileExporter extends FileProcessor {
    
    public XmlFileExporter(Handler updateHandler, Database database) {
        super(updateHandler, database);
    }

    @Override
    public void processFile(String filePath) {
        PrintWriter pw = null;
        try {
            ArrayList<GroupInfo> groupInfoList = mDatabase.getGroups();
            ArrayList<ToDoInfo> now = mDatabase.getAllGroups(STATUS_NOW, false);
            ArrayList<ToDoInfo> later = mDatabase.getAllGroups(STATUS_LATER, false);
            ArrayList<ToDoInfo> done = mDatabase.getAllGroups(STATUS_DONE, false);

            int total = groupInfoList.size() + now.size() + later.size() + done.size();
            sendMessageStarted(total);
            pw = new PrintWriter(new File(filePath));

            pw.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            pw.println("<todolist>");
            pw.println("<status id=\"0\">now</status>");
            pw.println("<status id=\"1\">later</status>");
            pw.println("<status id=\"2\">done</status>");
            exportGroups(pw, groupInfoList);
            exportToDOs(pw, now);
            exportToDOs(pw, later);
            exportToDOs(pw, done);
            pw.println("</todolist>");

            sendMessageCompleted();
            pw.close();
        }
        catch (Exception e) {
            sendMessageFailed(e.getMessage());
            if (pw != null) {
                pw.close();
            }
        }
    }

    private void exportGroups(PrintWriter pw, ArrayList<GroupInfo> groupInfoList) {
        for (GroupInfo groupInfo : groupInfoList) {
            long groupId = groupInfo.getId();
            if (groupId > 1) {
                pw.print("<group id=\"" + groupId + "\">");
                pw.print("<![CDATA[" + groupInfo.getGroupName() + "]]>");
                pw.println("</group>");
            }
            sendMessageUpdated();
        }
    }

    private void exportToDOs(PrintWriter pw, ArrayList<ToDoInfo> toDoInfoList) {
        for (ToDoInfo toDoInfo : toDoInfoList) {
            pw.print("<toDo id=\"" + toDoInfo.getId());
            pw.print("\" group=\"" + toDoInfo.getGroup());
            pw.print("\" status=\"" + toDoInfo.getStatus());
            pw.print("\" date=\"" + toDoInfo.getDate());
            pw.print("\" created=\"" + toDoInfo.getDateCreated());
            pw.println("\">");
            pw.println("<![CDATA[");
            pw.println(toDoInfo.getDetail());
            pw.println("]]>");
            pw.println("</toDo>");
            sendMessageUpdated();
        }
    }
}
