package edu.illinois.ncsa.bard.ui.query;

import org.tupeloproject.kernel.OperatorException;

public abstract class BardQuery
{
    public BardQuery()
    {
    }

    public abstract void execute() throws OperatorException;
}
