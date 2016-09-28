begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParseField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParseFieldMatcherSupplier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesReference
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|ObjectParser
operator|.
name|ValueType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
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
name|ArrayList
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
name|function
operator|.
name|BiConsumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiFunction
import|;
end_import

begin_comment
comment|/**  * Superclass for {@link ObjectParser} and {@link ConstructingObjectParser}. Defines most of the "declare" methods so they can be shared.  */
end_comment

begin_class
DECL|class|AbstractObjectParser
specifier|public
specifier|abstract
class|class
name|AbstractObjectParser
parameter_list|<
name|Value
parameter_list|,
name|Context
extends|extends
name|ParseFieldMatcherSupplier
parameter_list|>
implements|implements
name|BiFunction
argument_list|<
name|XContentParser
argument_list|,
name|Context
argument_list|,
name|Value
argument_list|>
block|{
comment|/**      * Reads an object from a parser using some context.      */
annotation|@
name|FunctionalInterface
DECL|interface|ContextParser
specifier|public
interface|interface
name|ContextParser
parameter_list|<
name|Context
parameter_list|,
name|T
parameter_list|>
block|{
DECL|method|parse
name|T
name|parse
parameter_list|(
name|XContentParser
name|p
parameter_list|,
name|Context
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**      * Reads an object right from the parser without any context.      */
annotation|@
name|FunctionalInterface
DECL|interface|NoContextParser
specifier|public
interface|interface
name|NoContextParser
parameter_list|<
name|T
parameter_list|>
block|{
DECL|method|parse
name|T
name|parse
parameter_list|(
name|XContentParser
name|p
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**      * Declare some field. Usually it is easier to use {@link #declareString(BiConsumer, ParseField)} or      * {@link #declareObject(BiConsumer, BiFunction, ParseField)} rather than call this directly.      */
DECL|method|declareField
specifier|public
specifier|abstract
parameter_list|<
name|T
parameter_list|>
name|void
name|declareField
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|T
argument_list|>
name|consumer
parameter_list|,
name|ContextParser
argument_list|<
name|Context
argument_list|,
name|T
argument_list|>
name|parser
parameter_list|,
name|ParseField
name|parseField
parameter_list|,
name|ValueType
name|type
parameter_list|)
function_decl|;
DECL|method|declareField
specifier|public
parameter_list|<
name|T
parameter_list|>
name|void
name|declareField
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|T
argument_list|>
name|consumer
parameter_list|,
name|NoContextParser
argument_list|<
name|T
argument_list|>
name|parser
parameter_list|,
name|ParseField
name|parseField
parameter_list|,
name|ValueType
name|type
parameter_list|)
block|{
if|if
condition|(
name|parser
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[parser] is required"
argument_list|)
throw|;
block|}
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parser
operator|.
name|parse
argument_list|(
name|p
argument_list|)
argument_list|,
name|parseField
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
DECL|method|declareObject
specifier|public
parameter_list|<
name|T
parameter_list|>
name|void
name|declareObject
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|T
argument_list|>
name|consumer
parameter_list|,
name|BiFunction
argument_list|<
name|XContentParser
argument_list|,
name|Context
argument_list|,
name|T
argument_list|>
name|objectParser
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|objectParser
operator|.
name|apply
argument_list|(
name|p
argument_list|,
name|c
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
block|}
DECL|method|declareFloat
specifier|public
name|void
name|declareFloat
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|Float
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
comment|// Using a method reference here angers some compilers
name|declareField
argument_list|(
name|consumer
argument_list|,
name|p
lambda|->
name|p
operator|.
name|floatValue
argument_list|()
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|FLOAT
argument_list|)
expr_stmt|;
block|}
DECL|method|declareDouble
specifier|public
name|void
name|declareDouble
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|Double
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
comment|// Using a method reference here angers some compilers
name|declareField
argument_list|(
name|consumer
argument_list|,
name|p
lambda|->
name|p
operator|.
name|doubleValue
argument_list|()
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|DOUBLE
argument_list|)
expr_stmt|;
block|}
DECL|method|declareLong
specifier|public
name|void
name|declareLong
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|Long
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
comment|// Using a method reference here angers some compilers
name|declareField
argument_list|(
name|consumer
argument_list|,
name|p
lambda|->
name|p
operator|.
name|longValue
argument_list|()
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|LONG
argument_list|)
expr_stmt|;
block|}
DECL|method|declareInt
specifier|public
name|void
name|declareInt
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|Integer
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
comment|// Using a method reference here angers some compilers
name|declareField
argument_list|(
name|consumer
argument_list|,
name|p
lambda|->
name|p
operator|.
name|intValue
argument_list|()
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|INT
argument_list|)
expr_stmt|;
block|}
DECL|method|declareString
specifier|public
name|void
name|declareString
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|String
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
name|XContentParser
operator|::
name|text
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|STRING
argument_list|)
expr_stmt|;
block|}
DECL|method|declareStringOrNull
specifier|public
name|void
name|declareStringOrNull
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|String
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
operator|(
name|p
operator|)
operator|->
name|p
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NULL
condition|?
literal|null
else|:
name|p
operator|.
name|text
argument_list|()
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|STRING_OR_NULL
argument_list|)
expr_stmt|;
block|}
DECL|method|declareBoolean
specifier|public
name|void
name|declareBoolean
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|Boolean
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
name|XContentParser
operator|::
name|booleanValue
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|BOOLEAN
argument_list|)
expr_stmt|;
block|}
DECL|method|declareObjectArray
specifier|public
parameter_list|<
name|T
parameter_list|>
name|void
name|declareObjectArray
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|List
argument_list|<
name|T
argument_list|>
argument_list|>
name|consumer
parameter_list|,
name|BiFunction
argument_list|<
name|XContentParser
argument_list|,
name|Context
argument_list|,
name|T
argument_list|>
name|objectParser
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parseArray
argument_list|(
name|p
argument_list|,
parameter_list|()
lambda|->
name|objectParser
operator|.
name|apply
argument_list|(
name|p
argument_list|,
name|c
argument_list|)
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|OBJECT_ARRAY
argument_list|)
expr_stmt|;
block|}
DECL|method|declareStringArray
specifier|public
name|void
name|declareStringArray
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parseArray
argument_list|(
name|p
argument_list|,
name|p
operator|::
name|text
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|STRING_ARRAY
argument_list|)
expr_stmt|;
block|}
DECL|method|declareDoubleArray
specifier|public
name|void
name|declareDoubleArray
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|List
argument_list|<
name|Double
argument_list|>
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parseArray
argument_list|(
name|p
argument_list|,
name|p
operator|::
name|doubleValue
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|DOUBLE_ARRAY
argument_list|)
expr_stmt|;
block|}
DECL|method|declareFloatArray
specifier|public
name|void
name|declareFloatArray
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|List
argument_list|<
name|Float
argument_list|>
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parseArray
argument_list|(
name|p
argument_list|,
name|p
operator|::
name|floatValue
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|FLOAT_ARRAY
argument_list|)
expr_stmt|;
block|}
DECL|method|declareLongArray
specifier|public
name|void
name|declareLongArray
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|List
argument_list|<
name|Long
argument_list|>
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parseArray
argument_list|(
name|p
argument_list|,
name|p
operator|::
name|longValue
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|LONG_ARRAY
argument_list|)
expr_stmt|;
block|}
DECL|method|declareIntArray
specifier|public
name|void
name|declareIntArray
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|declareField
argument_list|(
name|consumer
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|parseArray
argument_list|(
name|p
argument_list|,
name|p
operator|::
name|intValue
argument_list|)
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|INT_ARRAY
argument_list|)
expr_stmt|;
block|}
DECL|method|declareRawObject
specifier|public
name|void
name|declareRawObject
parameter_list|(
name|BiConsumer
argument_list|<
name|Value
argument_list|,
name|BytesReference
argument_list|>
name|consumer
parameter_list|,
name|ParseField
name|field
parameter_list|)
block|{
name|NoContextParser
argument_list|<
name|BytesReference
argument_list|>
name|bytesParser
init|=
name|p
lambda|->
block|{
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
init|)
block|{
name|builder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|p
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|bytes
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|declareField
argument_list|(
name|consumer
argument_list|,
name|bytesParser
argument_list|,
name|field
argument_list|,
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
block|}
DECL|interface|IOSupplier
specifier|private
interface|interface
name|IOSupplier
parameter_list|<
name|T
parameter_list|>
block|{
DECL|method|get
name|T
name|get
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
DECL|method|parseArray
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|parseArray
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|IOSupplier
argument_list|<
name|T
argument_list|>
name|supplier
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|T
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|.
name|isValue
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|supplier
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// single value
block|}
else|else
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|.
name|isValue
argument_list|()
operator|||
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|supplier
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"expected value but got ["
operator|+
name|parser
operator|.
name|currentToken
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|list
return|;
block|}
block|}
end_class

end_unit

