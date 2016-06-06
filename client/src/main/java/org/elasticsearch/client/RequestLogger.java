begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|Header
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpEntityEnclosingRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|methods
operator|.
name|HttpUriRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|BufferedHttpEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|ContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|util
operator|.
name|EntityUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_comment
comment|/**  * Helper class that exposes static methods to unify the way requests are logged.  * Includes trace logging to log complete requests and responses in curl format.  */
end_comment

begin_class
DECL|class|RequestLogger
specifier|final
class|class
name|RequestLogger
block|{
DECL|field|tracer
specifier|private
specifier|static
specifier|final
name|Log
name|tracer
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"tracer"
argument_list|)
decl_stmt|;
DECL|method|RequestLogger
specifier|private
name|RequestLogger
parameter_list|()
block|{     }
comment|/**      * Logs a request that yielded a response      */
DECL|method|log
specifier|static
name|void
name|log
parameter_list|(
name|Log
name|logger
parameter_list|,
name|String
name|message
parameter_list|,
name|HttpUriRequest
name|request
parameter_list|,
name|HttpHost
name|host
parameter_list|,
name|HttpResponse
name|httpResponse
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|message
operator|+
literal|" ["
operator|+
name|request
operator|.
name|getMethod
argument_list|()
operator|+
literal|" "
operator|+
name|host
operator|+
name|request
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
operator|+
literal|"] ["
operator|+
name|httpResponse
operator|.
name|getStatusLine
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tracer
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|String
name|requestLine
decl_stmt|;
try|try
block|{
name|requestLine
operator|=
name|buildTraceRequest
argument_list|(
name|request
argument_list|,
name|host
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|requestLine
operator|=
literal|""
expr_stmt|;
name|tracer
operator|.
name|trace
argument_list|(
literal|"error while reading request for trace purposes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|String
name|responseLine
decl_stmt|;
try|try
block|{
name|responseLine
operator|=
name|buildTraceResponse
argument_list|(
name|httpResponse
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|responseLine
operator|=
literal|""
expr_stmt|;
name|tracer
operator|.
name|trace
argument_list|(
literal|"error while reading response for trace purposes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|tracer
operator|.
name|trace
argument_list|(
name|requestLine
operator|+
literal|'\n'
operator|+
name|responseLine
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Logs a request that failed      */
DECL|method|log
specifier|static
name|void
name|log
parameter_list|(
name|Log
name|logger
parameter_list|,
name|String
name|message
parameter_list|,
name|HttpUriRequest
name|request
parameter_list|,
name|HttpHost
name|host
parameter_list|,
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
name|message
operator|+
literal|" ["
operator|+
name|request
operator|.
name|getMethod
argument_list|()
operator|+
literal|" "
operator|+
name|host
operator|+
name|request
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|String
name|traceRequest
decl_stmt|;
try|try
block|{
name|traceRequest
operator|=
name|buildTraceRequest
argument_list|(
name|request
argument_list|,
name|host
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|tracer
operator|.
name|trace
argument_list|(
literal|"error while reading request for trace purposes"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|traceRequest
operator|=
literal|""
expr_stmt|;
block|}
name|tracer
operator|.
name|trace
argument_list|(
name|traceRequest
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Creates curl output for given request      */
DECL|method|buildTraceRequest
specifier|static
name|String
name|buildTraceRequest
parameter_list|(
name|HttpUriRequest
name|request
parameter_list|,
name|HttpHost
name|host
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|requestLine
init|=
literal|"curl -iX "
operator|+
name|request
operator|.
name|getMethod
argument_list|()
operator|+
literal|" '"
operator|+
name|host
operator|+
name|request
operator|.
name|getRequestLine
argument_list|()
operator|.
name|getUri
argument_list|()
operator|+
literal|"'"
decl_stmt|;
if|if
condition|(
name|request
operator|instanceof
name|HttpEntityEnclosingRequest
condition|)
block|{
name|HttpEntityEnclosingRequest
name|enclosingRequest
init|=
operator|(
name|HttpEntityEnclosingRequest
operator|)
name|request
decl_stmt|;
if|if
condition|(
name|enclosingRequest
operator|.
name|getEntity
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|requestLine
operator|+=
literal|" -d '"
expr_stmt|;
name|HttpEntity
name|entity
init|=
operator|new
name|BufferedHttpEntity
argument_list|(
name|enclosingRequest
operator|.
name|getEntity
argument_list|()
argument_list|)
decl_stmt|;
name|enclosingRequest
operator|.
name|setEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
name|requestLine
operator|+=
name|EntityUtils
operator|.
name|toString
argument_list|(
name|entity
argument_list|)
operator|+
literal|"'"
expr_stmt|;
block|}
block|}
return|return
name|requestLine
return|;
block|}
comment|/**      * Creates curl output for given response      */
DECL|method|buildTraceResponse
specifier|static
name|String
name|buildTraceResponse
parameter_list|(
name|HttpResponse
name|httpResponse
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|responseLine
init|=
literal|"# "
operator|+
name|httpResponse
operator|.
name|getStatusLine
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
for|for
control|(
name|Header
name|header
range|:
name|httpResponse
operator|.
name|getAllHeaders
argument_list|()
control|)
block|{
name|responseLine
operator|+=
literal|"\n# "
operator|+
name|header
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|header
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
name|responseLine
operator|+=
literal|"\n#"
expr_stmt|;
name|HttpEntity
name|entity
init|=
name|httpResponse
operator|.
name|getEntity
argument_list|()
decl_stmt|;
if|if
condition|(
name|entity
operator|!=
literal|null
condition|)
block|{
name|entity
operator|=
operator|new
name|BufferedHttpEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
name|httpResponse
operator|.
name|setEntity
argument_list|(
name|entity
argument_list|)
expr_stmt|;
name|ContentType
name|contentType
init|=
name|ContentType
operator|.
name|get
argument_list|(
name|entity
argument_list|)
decl_stmt|;
name|Charset
name|charset
init|=
name|StandardCharsets
operator|.
name|UTF_8
decl_stmt|;
if|if
condition|(
name|contentType
operator|!=
literal|null
condition|)
block|{
name|charset
operator|=
name|contentType
operator|.
name|getCharset
argument_list|()
expr_stmt|;
block|}
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|entity
operator|.
name|getContent
argument_list|()
argument_list|,
name|charset
argument_list|)
argument_list|)
init|)
block|{
name|String
name|line
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|responseLine
operator|+=
literal|"\n# "
operator|+
name|line
expr_stmt|;
block|}
block|}
block|}
return|return
name|responseLine
return|;
block|}
block|}
end_class

end_unit

