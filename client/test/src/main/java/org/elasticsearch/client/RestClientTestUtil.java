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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomNumbers
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomStrings
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
name|message
operator|.
name|BasicHeader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_class
DECL|class|RestClientTestUtil
specifier|final
class|class
name|RestClientTestUtil
block|{
DECL|field|HTTP_METHODS
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|HTTP_METHODS
init|=
operator|new
name|String
index|[]
block|{
literal|"DELETE"
block|,
literal|"HEAD"
block|,
literal|"GET"
block|,
literal|"OPTIONS"
block|,
literal|"PATCH"
block|,
literal|"POST"
block|,
literal|"PUT"
block|,
literal|"TRACE"
block|}
decl_stmt|;
DECL|field|ALL_STATUS_CODES
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|ALL_STATUS_CODES
decl_stmt|;
DECL|field|OK_STATUS_CODES
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|OK_STATUS_CODES
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|200
argument_list|,
literal|201
argument_list|)
decl_stmt|;
DECL|field|ALL_ERROR_STATUS_CODES
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|ALL_ERROR_STATUS_CODES
decl_stmt|;
DECL|field|ERROR_NO_RETRY_STATUS_CODES
specifier|private
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|ERROR_NO_RETRY_STATUS_CODES
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|400
argument_list|,
literal|401
argument_list|,
literal|403
argument_list|,
literal|404
argument_list|,
literal|405
argument_list|,
literal|500
argument_list|)
decl_stmt|;
DECL|field|ERROR_RETRY_STATUS_CODES
specifier|private
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|ERROR_RETRY_STATUS_CODES
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|502
argument_list|,
literal|503
argument_list|,
literal|504
argument_list|)
decl_stmt|;
static|static
block|{
name|ALL_ERROR_STATUS_CODES
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ERROR_RETRY_STATUS_CODES
argument_list|)
expr_stmt|;
name|ALL_ERROR_STATUS_CODES
operator|.
name|addAll
argument_list|(
name|ERROR_NO_RETRY_STATUS_CODES
argument_list|)
expr_stmt|;
name|ALL_STATUS_CODES
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ALL_ERROR_STATUS_CODES
argument_list|)
expr_stmt|;
name|ALL_STATUS_CODES
operator|.
name|addAll
argument_list|(
name|OK_STATUS_CODES
argument_list|)
expr_stmt|;
block|}
DECL|method|RestClientTestUtil
specifier|private
name|RestClientTestUtil
parameter_list|()
block|{      }
DECL|method|getHttpMethods
specifier|static
name|String
index|[]
name|getHttpMethods
parameter_list|()
block|{
return|return
name|HTTP_METHODS
return|;
block|}
DECL|method|randomHttpMethod
specifier|static
name|String
name|randomHttpMethod
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|HTTP_METHODS
argument_list|)
return|;
block|}
DECL|method|randomStatusCode
specifier|static
name|int
name|randomStatusCode
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|ALL_STATUS_CODES
argument_list|)
return|;
block|}
DECL|method|randomOkStatusCode
specifier|static
name|int
name|randomOkStatusCode
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|OK_STATUS_CODES
argument_list|)
return|;
block|}
DECL|method|randomErrorNoRetryStatusCode
specifier|static
name|int
name|randomErrorNoRetryStatusCode
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|ERROR_NO_RETRY_STATUS_CODES
argument_list|)
return|;
block|}
DECL|method|randomErrorRetryStatusCode
specifier|static
name|int
name|randomErrorRetryStatusCode
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
return|return
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|ERROR_RETRY_STATUS_CODES
argument_list|)
return|;
block|}
DECL|method|getOkStatusCodes
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|getOkStatusCodes
parameter_list|()
block|{
return|return
name|OK_STATUS_CODES
return|;
block|}
DECL|method|getAllErrorStatusCodes
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|getAllErrorStatusCodes
parameter_list|()
block|{
return|return
name|ALL_ERROR_STATUS_CODES
return|;
block|}
DECL|method|getAllStatusCodes
specifier|static
name|List
argument_list|<
name|Integer
argument_list|>
name|getAllStatusCodes
parameter_list|()
block|{
return|return
name|ALL_STATUS_CODES
return|;
block|}
comment|/**      * Create a random number of {@link Header}s.      * Generated header names will either be the {@code baseName} plus its index, or exactly the provided {@code baseName} so that the      * we test also support for multiple headers with same key and different values.      */
DECL|method|randomHeaders
specifier|static
name|Header
index|[]
name|randomHeaders
parameter_list|(
name|Random
name|random
parameter_list|,
specifier|final
name|String
name|baseName
parameter_list|)
block|{
name|int
name|numHeaders
init|=
name|RandomNumbers
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
specifier|final
name|Header
index|[]
name|headers
init|=
operator|new
name|Header
index|[
name|numHeaders
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
name|numHeaders
condition|;
name|i
operator|++
control|)
block|{
name|String
name|headerName
init|=
name|baseName
decl_stmt|;
comment|//randomly exercise the code path that supports multiple headers with same key
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|headerName
operator|=
name|headerName
operator|+
name|i
expr_stmt|;
block|}
name|headers
index|[
name|i
index|]
operator|=
operator|new
name|BasicHeader
argument_list|(
name|headerName
argument_list|,
name|RandomStrings
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
name|random
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|headers
return|;
block|}
block|}
end_class

end_unit

