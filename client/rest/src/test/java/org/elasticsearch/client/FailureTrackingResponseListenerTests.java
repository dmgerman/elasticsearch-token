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
name|ProtocolVersion
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
name|RequestLine
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
name|StatusLine
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
name|message
operator|.
name|BasicHttpResponse
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
name|message
operator|.
name|BasicRequestLine
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
name|message
operator|.
name|BasicStatusLine
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertSame
import|;
end_import

begin_class
DECL|class|FailureTrackingResponseListenerTests
specifier|public
class|class
name|FailureTrackingResponseListenerTests
extends|extends
name|RestClientTestCase
block|{
DECL|method|testOnSuccess
specifier|public
name|void
name|testOnSuccess
parameter_list|()
block|{
name|MockResponseListener
name|responseListener
init|=
operator|new
name|MockResponseListener
argument_list|()
decl_stmt|;
name|RestClient
operator|.
name|FailureTrackingResponseListener
name|listener
init|=
operator|new
name|RestClient
operator|.
name|FailureTrackingResponseListener
argument_list|(
name|responseListener
argument_list|)
decl_stmt|;
block|}
DECL|method|testOnFailure
specifier|public
name|void
name|testOnFailure
parameter_list|()
block|{
name|MockResponseListener
name|responseListener
init|=
operator|new
name|MockResponseListener
argument_list|()
decl_stmt|;
name|RestClient
operator|.
name|FailureTrackingResponseListener
name|listener
init|=
operator|new
name|RestClient
operator|.
name|FailureTrackingResponseListener
argument_list|(
name|responseListener
argument_list|)
decl_stmt|;
name|int
name|numIters
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Exception
index|[]
name|expectedExceptions
init|=
operator|new
name|Exception
index|[
name|numIters
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numIters
condition|;
name|i
operator|++
control|)
block|{
name|RuntimeException
name|runtimeException
init|=
operator|new
name|RuntimeException
argument_list|(
literal|"test"
operator|+
name|i
argument_list|)
decl_stmt|;
name|expectedExceptions
index|[
name|i
index|]
operator|=
name|runtimeException
expr_stmt|;
name|listener
operator|.
name|trackFailure
argument_list|(
name|runtimeException
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|responseListener
operator|.
name|response
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|responseListener
operator|.
name|exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Response
name|response
init|=
name|mockResponse
argument_list|()
decl_stmt|;
name|listener
operator|.
name|onSuccess
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|response
argument_list|,
name|responseListener
operator|.
name|response
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|responseListener
operator|.
name|exception
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|RuntimeException
name|runtimeException
init|=
operator|new
name|RuntimeException
argument_list|(
literal|"definitive"
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onDefinitiveFailure
argument_list|(
name|runtimeException
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|responseListener
operator|.
name|response
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|Throwable
name|exception
init|=
name|responseListener
operator|.
name|exception
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSame
argument_list|(
name|runtimeException
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|int
name|i
init|=
name|numIters
operator|-
literal|1
decl_stmt|;
do|do
block|{
name|assertNotNull
argument_list|(
name|exception
operator|.
name|getSuppressed
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|exception
operator|.
name|getSuppressed
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|expectedExceptions
index|[
name|i
operator|--
index|]
argument_list|,
name|exception
operator|.
name|getSuppressed
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|exception
operator|=
name|exception
operator|.
name|getSuppressed
argument_list|()
index|[
literal|0
index|]
expr_stmt|;
block|}
do|while
condition|(
name|i
operator|>=
literal|0
condition|)
do|;
block|}
block|}
DECL|class|MockResponseListener
specifier|private
specifier|static
class|class
name|MockResponseListener
implements|implements
name|ResponseListener
block|{
DECL|field|response
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Response
argument_list|>
name|response
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|exception
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|exception
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|onSuccess
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|response
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|response
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"onSuccess was called multiple times"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|exception
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|exception
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"onFailure was called multiple times"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|mockResponse
specifier|private
specifier|static
name|Response
name|mockResponse
parameter_list|()
block|{
name|ProtocolVersion
name|protocolVersion
init|=
operator|new
name|ProtocolVersion
argument_list|(
literal|"HTTP"
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|RequestLine
name|requestLine
init|=
operator|new
name|BasicRequestLine
argument_list|(
literal|"GET"
argument_list|,
literal|"/"
argument_list|,
name|protocolVersion
argument_list|)
decl_stmt|;
name|StatusLine
name|statusLine
init|=
operator|new
name|BasicStatusLine
argument_list|(
name|protocolVersion
argument_list|,
literal|200
argument_list|,
literal|"OK"
argument_list|)
decl_stmt|;
name|HttpResponse
name|httpResponse
init|=
operator|new
name|BasicHttpResponse
argument_list|(
name|statusLine
argument_list|)
decl_stmt|;
return|return
operator|new
name|Response
argument_list|(
name|requestLine
argument_list|,
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|,
name|httpResponse
argument_list|)
return|;
block|}
block|}
end_class

end_unit

