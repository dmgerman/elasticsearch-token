begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|GrokTests
specifier|public
class|class
name|GrokTests
extends|extends
name|ESTestCase
block|{
DECL|field|basePatterns
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|basePatterns
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|basePatterns
operator|=
name|IngestCommonPlugin
operator|.
name|loadBuiltinPatterns
argument_list|()
expr_stmt|;
block|}
DECL|method|testMatchWithoutCaptures
specifier|public
name|void
name|testMatchWithoutCaptures
parameter_list|()
block|{
name|String
name|line
init|=
literal|"value"
decl_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
name|line
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|matches
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleSyslogLine
specifier|public
name|void
name|testSimpleSyslogLine
parameter_list|()
block|{
name|String
name|line
init|=
literal|"Mar 16 00:01:25 evita postfix/smtpd[1713]: connect from camomile.cloud9.net[168.100.1.3]"
decl_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"%{SYSLOGLINE}"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
name|line
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"evita"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"logsource"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Mar 16 00:01:25"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"connect from camomile.cloud9.net[168.100.1.3]"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"message"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"postfix/smtpd"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"program"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1713"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"pid"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSyslog5424Line
specifier|public
name|void
name|testSyslog5424Line
parameter_list|()
block|{
name|String
name|line
init|=
literal|"<191>1 2009-06-30T18:30:00+02:00 paxton.local grokdebug 4123 - [id1 foo=\\\"bar\\\"][id2 baz=\\\"something\\\"] "
operator|+
literal|"Hello, syslog."
decl_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"%{SYSLOG5424LINE}"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
name|line
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"191"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_pri"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_ver"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2009-06-30T18:30:00+02:00"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_ts"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"paxton.local"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_host"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"grokdebug"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_app"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4123"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_proc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_msgid"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"[id1 foo=\\\"bar\\\"][id2 baz=\\\"something\\\"]"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_sd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Hello, syslog."
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"syslog5424_msg"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDatePattern
specifier|public
name|void
name|testDatePattern
parameter_list|()
block|{
name|String
name|line
init|=
literal|"fancy 12-12-12 12:12:12"
decl_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"(?<timestamp>%{DATE_EU} %{TIME})"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
name|line
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"12-12-12 12:12:12"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNilCoercedValues
specifier|public
name|void
name|testNilCoercedValues
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"test (N/A|%{BASE10NUM:duration:float}ms)"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
literal|"test 28.4ms"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|28.4f
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"duration"
argument_list|)
argument_list|)
expr_stmt|;
name|matches
operator|=
name|grok
operator|.
name|captures
argument_list|(
literal|"test N/A"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"duration"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNilWithNoCoercion
specifier|public
name|void
name|testNilWithNoCoercion
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"test (N/A|%{BASE10NUM:duration}ms)"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
literal|"test 28.4ms"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"28.4"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"duration"
argument_list|)
argument_list|)
expr_stmt|;
name|matches
operator|=
name|grok
operator|.
name|captures
argument_list|(
literal|"test N/A"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"duration"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnicodeSyslog
specifier|public
name|void
name|testUnicodeSyslog
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"<%{POSINT:syslog_pri}>%{SPACE}%{SYSLOGTIMESTAMP:syslog_timestamp} "
operator|+
literal|"%{SYSLOGHOST:syslog_hostname} %{PROG:syslog_program}(:?)(?:\\[%{GREEDYDATA:syslog_pid}\\])?(:?) "
operator|+
literal|"%{GREEDYDATA:syslog_message}"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
literal|"<22>Jan  4 07:50:46 mailmaster postfix/policy-spf[9454]: : "
operator|+
literal|"SPF permerror (Junk encountered in record 'v=spf1 mx a:mail.domain.no ip4:192.168.0.4 ï¿½all'): Envelope-from: "
operator|+
literal|"email@domain.no"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|matches
operator|.
name|get
argument_list|(
literal|"syslog_pri"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"22"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|matches
operator|.
name|get
argument_list|(
literal|"syslog_program"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"postfix/policy-spf"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|matches
operator|.
name|get
argument_list|(
literal|"tags"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNamedFieldsWithWholeTextMatch
specifier|public
name|void
name|testNamedFieldsWithWholeTextMatch
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"%{DATE_EU:stimestamp}"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
literal|"11/01/01"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|matches
operator|.
name|get
argument_list|(
literal|"stimestamp"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"11/01/01"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testWithOniguramaNamedCaptures
specifier|public
name|void
name|testWithOniguramaNamedCaptures
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"(?<foo>\\w+)"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
literal|"hello world"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|matches
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"hello"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testISO8601
specifier|public
name|void
name|testISO8601
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"^%{TIMESTAMP_ISO8601}$"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|timeMessages
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2001-01-01T00:00:00"
argument_list|,
literal|"1974-03-02T04:09:09"
argument_list|,
literal|"2010-05-03T08:18:18+00:00"
argument_list|,
literal|"2004-07-04T12:27:27-00:00"
argument_list|,
literal|"2001-09-05T16:36:36+0000"
argument_list|,
literal|"2001-11-06T20:45:45-0000"
argument_list|,
literal|"2001-12-07T23:54:54Z"
argument_list|,
literal|"2001-01-01T00:00:00.123456"
argument_list|,
literal|"1974-03-02T04:09:09.123456"
argument_list|,
literal|"2010-05-03T08:18:18.123456+00:00"
argument_list|,
literal|"2004-07-04T12:27:27.123456-00:00"
argument_list|,
literal|"2001-09-05T16:36:36.123456+0000"
argument_list|,
literal|"2001-11-06T20:45:45.123456-0000"
argument_list|,
literal|"2001-12-07T23:54:54.123456Z"
argument_list|,
literal|"2001-12-07T23:54:60.123456Z"
comment|// '60' second is a leap second.
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|msg
range|:
name|timeMessages
control|)
block|{
name|assertThat
argument_list|(
name|grok
operator|.
name|match
argument_list|(
name|msg
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNotISO8601
specifier|public
name|void
name|testNotISO8601
parameter_list|()
block|{
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"^%{TIMESTAMP_ISO8601}$"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|timeMessages
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2001-13-01T00:00:00"
argument_list|,
comment|// invalid month
literal|"2001-00-01T00:00:00"
argument_list|,
comment|// invalid month
literal|"2001-01-00T00:00:00"
argument_list|,
comment|// invalid day
literal|"2001-01-32T00:00:00"
argument_list|,
comment|// invalid day
literal|"2001-01-aT00:00:00"
argument_list|,
comment|// invalid day
literal|"2001-01-1aT00:00:00"
argument_list|,
comment|// invalid day
literal|"2001-01-01Ta0:00:00"
argument_list|,
comment|// invalid hour
literal|"2001-01-01T0:00:00"
argument_list|,
comment|// invalid hour
literal|"2001-01-01T25:00:00"
argument_list|,
comment|// invalid hour
literal|"2001-01-01T01:60:00"
argument_list|,
comment|// invalid minute
literal|"2001-01-01T00:aa:00"
argument_list|,
comment|// invalid minute
literal|"2001-01-01T00:00:aa"
argument_list|,
comment|// invalid second
literal|"2001-01-01T00:00:-1"
argument_list|,
comment|// invalid second
literal|"2001-01-01T00:00:61"
argument_list|,
comment|// invalid second
literal|"2001-01-01T00:00:00A"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00+"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00+25"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00+2500"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00+25:00"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00-25"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00-2500"
argument_list|,
comment|// invalid timezone
literal|"2001-01-01T00:00:00-00:61"
comment|// invalid timezone
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|msg
range|:
name|timeMessages
control|)
block|{
name|assertThat
argument_list|(
name|grok
operator|.
name|match
argument_list|(
name|msg
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNoNamedCaptures
specifier|public
name|void
name|testNoNamedCaptures
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"NAME"
argument_list|,
literal|"Tal"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"EXCITED_NAME"
argument_list|,
literal|"!!!%{NAME:name}!!!"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"TEST"
argument_list|,
literal|"hello world"
argument_list|)
expr_stmt|;
name|String
name|text
init|=
literal|"wowza !!!Tal!!! - Tal"
decl_stmt|;
name|String
name|pattern
init|=
literal|"%{EXCITED_NAME} - %{NAME}"
decl_stmt|;
name|Grok
name|g
init|=
operator|new
name|Grok
argument_list|(
name|bank
argument_list|,
name|pattern
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"(?<EXCITED_NAME_0>!!!(?<NAME_21>Tal)!!!) - (?<NAME_22>Tal)"
argument_list|,
name|g
operator|.
name|toRegex
argument_list|(
name|pattern
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|g
operator|.
name|match
argument_list|(
name|text
argument_list|)
argument_list|)
expr_stmt|;
name|Object
name|actual
init|=
name|g
operator|.
name|captures
argument_list|(
name|text
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expected
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"EXCITED_NAME_0"
argument_list|,
literal|"!!!Tal!!!"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"NAME_21"
argument_list|,
literal|"Tal"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"NAME_22"
argument_list|,
literal|"Tal"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|testNumericCapturesCoercion
specifier|public
name|void
name|testNumericCapturesCoercion
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"BASE10NUM"
argument_list|,
literal|"(?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\\.[0-9]+)?)|(?:\\.[0-9]+)))"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"NUMBER"
argument_list|,
literal|"(?:%{BASE10NUM})"
argument_list|)
expr_stmt|;
name|String
name|pattern
init|=
literal|"%{NUMBER:bytes:float} %{NUMBER:status} %{NUMBER}"
decl_stmt|;
name|Grok
name|g
init|=
operator|new
name|Grok
argument_list|(
name|bank
argument_list|,
name|pattern
argument_list|)
decl_stmt|;
name|String
name|text
init|=
literal|"12009.34 200 9032"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expected
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"bytes"
argument_list|,
literal|12009.34f
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"status"
argument_list|,
literal|"200"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actual
init|=
name|g
operator|.
name|captures
argument_list|(
name|text
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|testApacheLog
specifier|public
name|void
name|testApacheLog
parameter_list|()
block|{
name|String
name|logLine
init|=
literal|"31.184.238.164 - - [24/Jul/2014:05:35:37 +0530] \"GET /logs/access.log HTTP/1.0\" 200 69849 "
operator|+
literal|"\"http://8rursodiol.enjin.com\" \"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) "
operator|+
literal|"Chrome/30.0.1599.12785 YaBrowser/13.12.1599.12785 Safari/537.36\" \"www.dlwindianrailways.com\""
decl_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|basePatterns
argument_list|,
literal|"%{COMBINEDAPACHELOG}"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
name|logLine
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"31.184.238.164"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"clientip"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"ident"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"auth"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"24/Jul/2014:05:35:37 +0530"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"timestamp"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"GET"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"verb"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"/logs/access.log"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"request"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1.0"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"httpversion"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"200"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"response"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"69849"
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"bytes"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"http://8rursodiol.enjin.com\""
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"referrer"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"port"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.12785 "
operator|+
literal|"YaBrowser/13.12.1599.12785 Safari/537.36\""
argument_list|,
name|matches
operator|.
name|get
argument_list|(
literal|"agent"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testComplete
specifier|public
name|void
name|testComplete
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"MONTHDAY"
argument_list|,
literal|"(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"MONTH"
argument_list|,
literal|"\\b(?:Jan(?:uary|uar)?|Feb(?:ruary|ruar)?|M(?:a|Ã¤)?r(?:ch|z)?|Apr(?:il)?|Ma(?:y|i)?|Jun(?:e|i)"
operator|+
literal|"?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|O(?:c|k)?t(?:ober)?|Nov(?:ember)?|De(?:c|z)(?:ember)?)\\b"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"MINUTE"
argument_list|,
literal|"(?:[0-5][0-9])"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"YEAR"
argument_list|,
literal|"(?>\\d\\d){1,2}"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"HOUR"
argument_list|,
literal|"(?:2[0123]|[01]?[0-9])"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"SECOND"
argument_list|,
literal|"(?:(?:[0-5]?[0-9]|60)(?:[:.,][0-9]+)?)"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"TIME"
argument_list|,
literal|"(?!<[0-9])%{HOUR}:%{MINUTE}(?::%{SECOND})(?![0-9])"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"INT"
argument_list|,
literal|"(?:[+-]?(?:[0-9]+))"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"HTTPDATE"
argument_list|,
literal|"%{MONTHDAY}/%{MONTH}/%{YEAR}:%{TIME} %{INT}"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"WORD"
argument_list|,
literal|"\\b\\w+\\b"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"BASE10NUM"
argument_list|,
literal|"(?<![0-9.+-])(?>[+-]?(?:(?:[0-9]+(?:\\.[0-9]+)?)|(?:\\.[0-9]+)))"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"NUMBER"
argument_list|,
literal|"(?:%{BASE10NUM})"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"IPV6"
argument_list|,
literal|"((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]"
operator|+
literal|"\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4})"
operator|+
literal|"{1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:)"
operator|+
literal|"{4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\"
operator|+
literal|"d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]"
operator|+
literal|"\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4})"
operator|+
literal|"{1,5})"
operator|+
literal|"|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))"
operator|+
literal|"|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
operator|+
literal|"(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}"
operator|+
literal|":((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"IPV4"
argument_list|,
literal|"(?<![0-9])(?:(?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.]"
operator|+
literal|"(?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[.](?:[0-1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))(?![0-9])"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"IP"
argument_list|,
literal|"(?:%{IPV6}|%{IPV4})"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"HOSTNAME"
argument_list|,
literal|"\\b(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*(\\.?|\\b)"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"IPORHOST"
argument_list|,
literal|"(?:%{IP}|%{HOSTNAME})"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"USER"
argument_list|,
literal|"[a-zA-Z0-9._-]+"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"DATA"
argument_list|,
literal|".*?"
argument_list|)
expr_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"QS"
argument_list|,
literal|"(?>(?<!\\\\)(?>\"(?>\\\\.|[^\\\\\"]+)+\"|\"\"|(?>'(?>\\\\.|[^\\\\']+)+')|''|(?>`(?>\\\\.|[^\\\\`]+)+`)|``))"
argument_list|)
expr_stmt|;
name|String
name|text
init|=
literal|"83.149.9.216 - - [19/Jul/2015:08:13:42 +0000] \"GET /presentations/logstash-monitorama-2013/images/"
operator|+
literal|"kibana-dashboard3.png HTTP/1.1\" 200 171717 \"http://semicomplete.com/presentations/logstash-monitorama-2013/\" "
operator|+
literal|"\"Mozilla"
operator|+
literal|"/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.77 Safari/537.36\""
decl_stmt|;
name|String
name|pattern
init|=
literal|"%{IPORHOST:clientip} %{USER:ident} %{USER:auth} \\[%{HTTPDATE:timestamp}\\] \"%{WORD:verb} %{DATA:request} "
operator|+
literal|"HTTP/%{NUMBER:httpversion}\" %{NUMBER:response:int} (?:-|%{NUMBER:bytes:int}) %{QS:referrer} %{QS:agent}"
decl_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|bank
argument_list|,
name|pattern
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expected
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"clientip"
argument_list|,
literal|"83.149.9.216"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"ident"
argument_list|,
literal|"-"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"auth"
argument_list|,
literal|"-"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"timestamp"
argument_list|,
literal|"19/Jul/2015:08:13:42 +0000"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"verb"
argument_list|,
literal|"GET"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"request"
argument_list|,
literal|"/presentations/logstash-monitorama-2013/images/kibana-dashboard3.png"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"httpversion"
argument_list|,
literal|"1.1"
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"response"
argument_list|,
literal|200
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"bytes"
argument_list|,
literal|171717
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"referrer"
argument_list|,
literal|"\"http://semicomplete.com/presentations/logstash-monitorama-2013/\""
argument_list|)
expr_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"agent"
argument_list|,
literal|"\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) "
operator|+
literal|"Chrome/32.0.1700.77 Safari/537.36\""
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actual
init|=
name|grok
operator|.
name|captures
argument_list|(
name|text
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoMatch
specifier|public
name|void
name|testNoMatch
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"MONTHDAY"
argument_list|,
literal|"(?:(?:0[1-9])|(?:[12][0-9])|(?:3[01])|[1-9])"
argument_list|)
expr_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|bank
argument_list|,
literal|"%{MONTHDAY:greatday}"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|grok
operator|.
name|captures
argument_list|(
literal|"nomatch"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultipleNamedCapturesWithSameName
specifier|public
name|void
name|testMultipleNamedCapturesWithSameName
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|bank
operator|.
name|put
argument_list|(
literal|"SINGLEDIGIT"
argument_list|,
literal|"[0-9]"
argument_list|)
expr_stmt|;
name|Grok
name|grok
init|=
operator|new
name|Grok
argument_list|(
name|bank
argument_list|,
literal|"%{SINGLEDIGIT:num}%{SINGLEDIGIT:num}"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expected
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|put
argument_list|(
literal|"num"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|grok
operator|.
name|captures
argument_list|(
literal|"12"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

