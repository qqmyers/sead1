//
//  MediciAppDelegate.h
//  Medici
//
//  Created by Rob Kooper on 9/8/10.
//  Copyright NCSA 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MediciViewController;

@interface MediciAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    MediciViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet MediciViewController *viewController;

@end

