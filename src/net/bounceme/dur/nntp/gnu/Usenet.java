package net.bounceme.dur.nntp.gnu;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

public enum Usenet {

    INSTANCE;
    private final Logger LOG = Logger.getLogger(Usenet.class.getName());
    private Properties props = new Properties();
    private Folder root = null;
    private Store store = null;
    private List<Folder> folders = new ArrayList<>();
    private Folder folder = null;

    Usenet() {
        LOG.fine("controller..");
        props = PropertiesReader.getProps();
        try {
            connect();
        } catch (Exception ex) {
            Logger.getLogger(Usenet.class.getName()).log(Level.SEVERE, "FAILED TO LOAD MESSAGES", ex);
        }
    }

    //hmm, is the property nntp.host correct?
    public void connect() throws Exception {
        LOG.fine("Usenet.connect..");
        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);
        store = session.getStore(new URLName(props.getProperty("nntp.host")));
        store.connect();
        root = store.getDefaultFolder();
        LOG.info("store is...\t" + store.toString());
        LOG.info("root is..\t" + root.toString());
        LOG.info("root size is..\t" + root.getFullName());
        LOG.info("root.listSubscribed are..\t" + root.listSubscribed().toString());
        setFolders(Arrays.asList(root.listSubscribed()));
    }

    public List<Message> getMessages(Page page) throws Exception {
        Newsgroup newsgroup = new Newsgroup(page);
        LOG.info("fetching.." + newsgroup);
        folder = root.getFolder(newsgroup.getNewsgroup());
        folder.open(Folder.READ_ONLY);
        List<Message> messages = Arrays.asList(folder.getMessages());
        LOG.info("..fetched " + folder);
        return Collections.unmodifiableList(messages);
    }

    public List<Folder> getFolders() {
        LOG.fine("folders " + folders);
        return Collections.unmodifiableList(folders);
    }

    private void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public Message getMessage(Newsgroup newsgroup, Article article) throws MessagingException {
        LOG.fine("\n\ntrying.." + newsgroup + article);
        String id = article.getMessageId();
        Message message = null;
        folder = root.getFolder(newsgroup.getNewsgroup());
        folder.open(Folder.READ_ONLY);
        SearchTerm st = new MessageIDTerm(id);
        List<Message> messages = Arrays.asList(folder.search(st));
        LOG.severe(messages.toString());
        if (!messages.isEmpty()) {
            message = messages.get(0);
        }
        LOG.info(message.getSubject());
        return message;
    }
}
