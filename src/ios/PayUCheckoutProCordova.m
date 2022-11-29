/********* payubiz.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <PayUCheckoutProKit/PayUCheckoutProKit.h>
#import <PayUCheckoutProBaseKit/PayUCheckoutProBaseKit.h>
#import <PayUParamsKit/PayUParamsKit.h>
#import <PayUBizCoreKit/PayUBizCoreKit.h>

@interface PayUCheckoutProCordova : CDVPlugin <PayUCheckoutProDelegate>

@property (nonatomic, strong) PayUHybridCheckoutProDelegateResponseTransformer *responseTransformer;
- (void)openCheckoutScreen:(CDVInvokedUrlCommand*)command;
- (void)hashGenerated:(CDVInvokedUrlCommand*)command;
@end

@implementation PayUCheckoutProCordova


#pragma mark Helper Methods
static NSString* myAsyncCallbackId = nil;


- (void)openCheckoutScreen:(CDVInvokedUrlCommand*)command {
    [CDVPluginResult setVerbose:true];
    myAsyncCallbackId = command.callbackId;
    dispatch_async(dispatch_get_main_queue(), ^{
        NSDictionary *params = command.arguments.firstObject;
        UIViewController *controller = [UIApplication sharedApplication].keyWindow.rootViewController;
        self.responseTransformer = [PayUHybridCheckoutProDelegateResponseTransformer new];
        [PayUCheckoutPro openOn:controller params:params delegate:self];
    });
}


- (void)hashGenerated:(CDVInvokedUrlCommand*)command{
    if(command.arguments.count > 0){
        [self.responseTransformer hashGeneratedWithArgs:command.arguments.firstObject errorCallback:^(NSError * _Nullable error) {
            if (error)
                [self onError:error];
        }];
    }else{
        NSError *err = [[NSError alloc] initWithDomain:@"" code:101 userInfo:[NSDictionary dictionaryWithObjectsAndKeys:@"Something went wrong", "errorMsg", nil]];
        [self onError:err];
    }
}

- (void)onPaymentSuccessWithResponse:(id _Nullable)response {
    // handle success scenario
    [self sendResultBack:[NSDictionary dictionaryWithObjectsAndKeys:
                          [self.responseTransformer onPaymentSuccessWithResponse:response],
                          PayUHybridCheckoutProDelegateResponseMethodName.onPaymentSuccess,
                          nil]];
}

- (void)onPaymentFailureWithResponse:(id _Nullable)response {
    // handle failure scenario
    [self sendResultBack:[NSDictionary dictionaryWithObjectsAndKeys:
                          [self.responseTransformer onPaymentFailureWithResponse:response],
                          PayUHybridCheckoutProDelegateResponseMethodName.onPaymentFailure,
                          nil]];
}

- (void)onError:(NSError * _Nullable)error {
    // handle error scenario
    [self sendResultBack:[NSDictionary dictionaryWithObjectsAndKeys:
                          [self.responseTransformer onError:error],
                          PayUHybridCheckoutProDelegateResponseMethodName.onError,
                          nil]];
}

- (void)onPaymentCancelWithIsTxnInitiated:(BOOL)isTxnInitiated {
    // handle txn cancelled scenario
    // isTxnInitiated == YES, means user cancelled the txn when on reaching bankPage
    // isTxnInitiated == NO, means user cancelled the txn before reaching the bankPage
    [self sendResultBack:[NSDictionary dictionaryWithObjectsAndKeys:
                          [self.responseTransformer onPaymentCancelWithIsTxnInitiated:isTxnInitiated], PayUHybridCheckoutProDelegateResponseMethodName.onPaymentCancel,
                          nil]];
}

- (void)generateHashFor:(NSDictionary<NSString *,NSString *> * _Nonnull)param onCompletion:(void (^ _Nonnull)(NSDictionary<NSString *,NSString *> * _Nonnull))onCompletion
{
    [self sendResultBack:[NSDictionary dictionaryWithObjectsAndKeys:
                          [self.responseTransformer generateHashFor:param onCompletion:onCompletion], PayUHybridCheckoutProDelegateResponseMethodName.generateHash,
                          nil]];
}

-(void)sendResultBack:(NSDictionary<NSString *,NSString *> * _Nonnull)response {
    [self.commandDelegate runInBackground:^{
        CDVPluginResult *result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsDictionary:response];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:myAsyncCallbackId];
    }];
}

@end

