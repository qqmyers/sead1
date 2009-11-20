package edu.illinois.ncsa.bard.ui.services.predicate;

import org.eclipse.swt.graphics.Image;
import org.tupeloproject.rdf.Resource;

public interface IPredicateImageService
{
    Image getImage( Resource predicate, Resource value );
}
