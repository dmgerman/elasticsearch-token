begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
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
name|inject
operator|.
name|internal
operator|.
name|Errors
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
name|inject
operator|.
name|internal
operator|.
name|MatcherAndConverter
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
name|inject
operator|.
name|internal
operator|.
name|SourceProvider
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
name|inject
operator|.
name|internal
operator|.
name|Strings
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
name|inject
operator|.
name|matcher
operator|.
name|AbstractMatcher
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
name|inject
operator|.
name|matcher
operator|.
name|Matcher
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
name|inject
operator|.
name|matcher
operator|.
name|Matchers
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
name|inject
operator|.
name|spi
operator|.
name|TypeConverter
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
name|inject
operator|.
name|spi
operator|.
name|TypeConverterBinding
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
import|;
end_import

begin_comment
comment|/**  * Handles {@link Binder#convertToTypes} commands.  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|TypeConverterBindingProcessor
class|class
name|TypeConverterBindingProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|method|TypeConverterBindingProcessor
name|TypeConverterBindingProcessor
parameter_list|(
name|Errors
name|errors
parameter_list|)
block|{
name|super
argument_list|(
name|errors
argument_list|)
expr_stmt|;
block|}
comment|/**      * Installs default converters for primitives, enums, and class literals.      */
DECL|method|prepareBuiltInConverters
specifier|public
name|void
name|prepareBuiltInConverters
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|)
block|{
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
try|try
block|{
comment|// Configure type converters.
name|convertToPrimitiveType
argument_list|(
name|int
operator|.
name|class
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToPrimitiveType
argument_list|(
name|long
operator|.
name|class
argument_list|,
name|Long
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToPrimitiveType
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
name|Boolean
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToPrimitiveType
argument_list|(
name|byte
operator|.
name|class
argument_list|,
name|Byte
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToPrimitiveType
argument_list|(
name|short
operator|.
name|class
argument_list|,
name|Short
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToPrimitiveType
argument_list|(
name|float
operator|.
name|class
argument_list|,
name|Float
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToPrimitiveType
argument_list|(
name|double
operator|.
name|class
argument_list|,
name|Double
operator|.
name|class
argument_list|)
expr_stmt|;
name|convertToClass
argument_list|(
name|Character
operator|.
name|class
argument_list|,
operator|new
name|TypeConverter
argument_list|()
block|{
specifier|public
name|Object
name|convert
parameter_list|(
name|String
name|value
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|toType
parameter_list|)
block|{
name|value
operator|=
name|value
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|value
operator|.
name|length
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Length != 1."
argument_list|)
throw|;
block|}
return|return
name|value
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TypeConverter<Character>"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|convertToClasses
argument_list|(
name|Matchers
operator|.
name|subclassesOf
argument_list|(
name|Enum
operator|.
name|class
argument_list|)
argument_list|,
operator|new
name|TypeConverter
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|convert
parameter_list|(
name|String
name|value
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|toType
parameter_list|)
block|{
return|return
name|Enum
operator|.
name|valueOf
argument_list|(
operator|(
name|Class
operator|)
name|toType
operator|.
name|getRawType
argument_list|()
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TypeConverter<E extends Enum<E>>"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|internalConvertToTypes
argument_list|(
operator|new
name|AbstractMatcher
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|boolean
name|matches
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
parameter_list|)
block|{
return|return
name|typeLiteral
operator|.
name|getRawType
argument_list|()
operator|==
name|Class
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Class<?>"
return|;
block|}
block|}
operator|,
operator|new
name|TypeConverter
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|convert
parameter_list|(
name|String
name|value
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|toType
parameter_list|)
block|{
try|try
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
name|value
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TypeConverter<Class<?>>"
return|;
block|}
block|}
block|)
empty_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|injector
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

begin_function
DECL|method|convertToPrimitiveType
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|convertToPrimitiveType
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|primitiveType
parameter_list|,
specifier|final
name|Class
argument_list|<
name|T
argument_list|>
name|wrapperType
parameter_list|)
block|{
try|try
block|{
specifier|final
name|Method
name|parser
init|=
name|wrapperType
operator|.
name|getMethod
argument_list|(
literal|"parse"
operator|+
name|Strings
operator|.
name|capitalize
argument_list|(
name|primitiveType
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|TypeConverter
name|typeConverter
init|=
operator|new
name|TypeConverter
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|convert
parameter_list|(
name|String
name|value
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|toType
parameter_list|)
block|{
try|try
block|{
return|return
name|parser
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|value
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getTargetException
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TypeConverter<"
operator|+
name|wrapperType
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|">"
return|;
block|}
block|}
decl_stmt|;
name|convertToClass
argument_list|(
name|wrapperType
argument_list|,
name|typeConverter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
DECL|method|convertToClass
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|convertToClass
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|,
name|TypeConverter
name|converter
parameter_list|)
block|{
name|convertToClasses
argument_list|(
name|Matchers
operator|.
name|identicalTo
argument_list|(
name|type
argument_list|)
argument_list|,
name|converter
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|convertToClasses
specifier|private
name|void
name|convertToClasses
parameter_list|(
specifier|final
name|Matcher
argument_list|<
name|?
super|super
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|typeMatcher
parameter_list|,
name|TypeConverter
name|converter
parameter_list|)
block|{
name|internalConvertToTypes
argument_list|(
operator|new
name|AbstractMatcher
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|boolean
name|matches
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
parameter_list|)
block|{
name|Type
name|type
init|=
name|typeLiteral
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|type
operator|instanceof
name|Class
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|type
decl_stmt|;
return|return
name|typeMatcher
operator|.
name|matches
argument_list|(
name|clazz
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|typeMatcher
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
operator|,
name|converter
block|)
function|;
end_function

begin_function
unit|}      private
DECL|method|internalConvertToTypes
name|void
name|internalConvertToTypes
parameter_list|(
name|Matcher
argument_list|<
name|?
super|super
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|typeMatcher
parameter_list|,
name|TypeConverter
name|converter
parameter_list|)
block|{
name|injector
operator|.
name|state
operator|.
name|addConverter
argument_list|(
operator|new
name|MatcherAndConverter
argument_list|(
name|typeMatcher
argument_list|,
name|converter
argument_list|,
name|SourceProvider
operator|.
name|UNKNOWN_SOURCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|visit
annotation|@
name|Override
specifier|public
name|Boolean
name|visit
parameter_list|(
name|TypeConverterBinding
name|command
parameter_list|)
block|{
name|injector
operator|.
name|state
operator|.
name|addConverter
argument_list|(
operator|new
name|MatcherAndConverter
argument_list|(
name|command
operator|.
name|getTypeMatcher
argument_list|()
argument_list|,
name|command
operator|.
name|getTypeConverter
argument_list|()
argument_list|,
name|command
operator|.
name|getSource
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
end_function

unit|}
end_unit

