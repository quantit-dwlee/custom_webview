import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

const String _viewType = 'FlutterCustomWebView';

enum CallMethod { post, get }

class CustomWebView extends StatefulWidget {
  const CustomWebView({
    super.key,
    this.header,
    this.body,
    this.callMethod = CallMethod.get,
    required this.url,
  });

  final Map<String, String>? header;
  final Map<String, String>? body;
  final String url;
  final CallMethod callMethod;

  @override
  State<CustomWebView> createState() => _CustomWebViewState();
}

class _CustomWebViewState extends State<CustomWebView> {
  String callbackUrl = 'Awaiting...';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

// Async method for setting up
  Future<void> initPlatformState() async {
    const stream = EventChannel("flutter.webview.eventChannel");
    stream
        .receiveBroadcastStream()
        .listen((event) => onData(event), onError: onError);
  }

  void onData(Object url) {
    print("*****************************");
    print(url.toString());
    print("*****************************");

    setState(() {
      callbackUrl = url.toString();
    });
  }

  void onError(Object error) {
    print('Something went wrong!');
  }

  @override
  Widget build(BuildContext context) {
    return PlatformViewLink(
      viewType: _viewType,
      surfaceFactory: (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: _viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: {
            "header": widget.header ?? {},
            "body": widget.body ?? {},
            "url": widget.url,
            "method": widget.callMethod == CallMethod.post ? "post" : "get",
          },
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}
