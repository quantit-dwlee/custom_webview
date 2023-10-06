import 'package:custom_webview/custom_webview.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: const CustomWebView(
          callMethod: CallMethod.post,
          body: {
            "ptnCd": "31",
            "callbackType": "REST",
            "callbackUrl": "app://testCallback",
            "ciNo":
                "1dvdaL2aEP/8slqVaHzMAVKlCV/2E3giXVyiLlhgSosj5ZTWNHIUJJltQn9qZKfT0/DSN54tirStM0cG3yWJuQ==",
            "channel": "baas",
            "ts": "20231006141253",
            "authKey":
                "GWePDiyQWfBgHDdUuX6eWmlAxE3t8Kina0ZGnXMYb3I5K/iS3X36sXLqRu3CKsxQ90Vv7jRn3/F1L3IV/p6esA==",
            "hsKey":
                "ZDkwMGUxMmE5ZTQ2NTE2OTZjMTk1Yzk1N2E5N2NkYjlkZGJhN2IwMjBjNTZlZWQwYTgwNTRmMTg1Mjg2MDE5MQ==",
            "linkUrl": "https://dbaasviewapi.kbsec.com/go.able?linkcd=p010101",
            "paramValue":
                "eyJjaU5vIjoiMWR2ZGFMMmFFUFwvOHNscVZhSHpNQVZLbENWXC8yRTNnaVhWeWlMbGhnU29zajVaVFdOSElVSkpsdFFuOXFaS2ZUMFwvRFNONTR0aXJTdE0wY0czeVdKdVE9PSIsImNhbGxiYWNrVXJsIjoiYXBwOlwvXC90ZXN0Q2FsbGJhY2siLCJwdG5DZCI6IjMxIiwiY2FsbGJhY2tUeXBlIjoiUkVTVCJ9",
            "issueNo": "20231006141253875",
          },
          header: {
            "Authorization": "bearer 6aadceb792dadd000eba2841c0bb520188d39bef",
            "User-Agent":
                "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",
          },
          url: "https://dbaasviewapi.kbsec.com/go.able?linkcd=p010101",
        ),
      ),
    );
  }
}
