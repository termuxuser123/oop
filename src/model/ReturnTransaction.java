package model;

import java.time.LocalDate;

public final class ReturnTransaction extends Transaction {
    private final IssueTransaction issueTxn;

    public ReturnTransaction(IssueTransaction orginal, LocalDate returnDate) {
        super(orginal.getUser(), orginal.getBook(), orginal.getIssueDate());
        this.issueTxn = orginal;
        setTxnId(orginal.getTxnId());
        setDueDate(orginal.getDueDate());
        setReturnDate(returnDate);
    }

    public IssueTransaction getIssueTxn() {
        return issueTxn;
    }

    @Override
    public void process() {
        getBook().checkInCopies();
    }
}
