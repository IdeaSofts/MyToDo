package com.idea.todo.wrapper.file;

import android.os.Handler;

import com.idea.todo.db.Database;
import com.idea.todo.model.GroupInfo;
import com.idea.todo.model.ToDoInfo;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

public class XmlFileImporter extends FileProcessor {
    private GroupInfo mCurrentGroupInfo;
    private ToDoInfo mCurrentToDoInfo;
    private String mCurrentText;
    private ArrayList<GroupInfo> mGroupInfoList;
    private ArrayList<ToDoInfo> toDoInfoList;

    public XmlFileImporter(Handler updateHandler, Database database) {
        super(updateHandler, database);
    }

    @Override
    public void processFile(String filePath) {
        try {
            XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            parser.setContentHandler(new XMLContentHandler());
            sendMessageStarted(3);
            init();
            sendMessageUpdated();

            parser.parse("file://" + filePath);
            sendMessageUpdated();

            mDatabase.importData(mGroupInfoList, toDoInfoList);
            sendMessageCompleted();

        } catch (Exception e) {
            e.printStackTrace();
            sendMessageFailed(e.getMessage());
        }
    }

    private void init() {
        mGroupInfoList = new ArrayList<>();
        toDoInfoList = new ArrayList<>();

        mCurrentGroupInfo = null;
        mCurrentToDoInfo = null;
        mCurrentText = "";
    }

    private class XMLContentHandler implements ContentHandler {

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            long id;
            if (localName.equals("group")) {
                id = getLongValue(attrs, "id");
                mCurrentGroupInfo = new GroupInfo();
                mCurrentGroupInfo.setId(id);
            }
            else if (localName.equals("toDo")) {
                id = getLongValue(attrs, "id");

                long group = getLongValue(attrs, "group");
                int status = (int) getLongValue(attrs, "status");
                long date = getLongValue(attrs, "date");
                long created = getLongValue(attrs, "created");

                mCurrentToDoInfo = new ToDoInfo();
                mCurrentToDoInfo.setId(id);
                mCurrentToDoInfo.setGroup(group);
                mCurrentToDoInfo.setStatus(status);
                mCurrentToDoInfo.setDate(date);
                mCurrentToDoInfo.setDateCreated(created);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals("group") && mCurrentGroupInfo != null) {
                mCurrentGroupInfo.setGroupName(mCurrentText.trim());
                mGroupInfoList.add(mCurrentGroupInfo);
                mCurrentGroupInfo = null;
                mCurrentText = "";
            }
            else if (localName.equals("toDo") && mCurrentToDoInfo != null) {
                mCurrentToDoInfo.setDetail(mCurrentText.trim());
                toDoInfoList.add(mCurrentToDoInfo);
                mCurrentToDoInfo = null;
                mCurrentText = "";
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String s = new String(ch, start, length);
            if (mCurrentGroupInfo != null || mCurrentToDoInfo != null) {
                mCurrentText += s;
            }
        }

        @Override
        public void startDocument() throws SAXException {}
        @Override
        public void skippedEntity(String name) throws SAXException {}
        @Override
        public void setDocumentLocator(Locator locator) {}
        @Override
        public void processingInstruction(String target, String data) throws SAXException {}
        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
        @Override
        public void endPrefixMapping(String prefix) throws SAXException {}
        @Override
        public void endDocument() throws SAXException {}
    }

    private long getLongValue(Attributes attrs, String name) {
        String value = attrs.getValue(name);
        return value == null ? 0 : Long.parseLong(value);
    }
}
