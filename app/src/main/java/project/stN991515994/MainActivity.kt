package project.stN991515994

import android.graphics.Color
import android.graphics.Typeface
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import project.stN991515994.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    //set up ViewBinding
    private lateinit var binding: ActivityMainBinding

    private var lastNumeric = false
    var stateError = false
    var lastDecimal = false

    //Use expression Builder which will help to look up and insert functions, operators,
    //constants, and identifiers (for example, field names, tables, forms,
    //and queries), saving time and reducing errors
    private lateinit var expression: Expression

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.radioButtonBlue.setTypeface(null, Typeface.BOLD)
        binding.radioButtonGreen.setTypeface(null, Typeface.BOLD)
        binding.radioButtonPink.setTypeface(null, Typeface.BOLD)
        binding.footerText.setTypeface(null, Typeface.BOLD)
        binding.author.setTypeface(null, Typeface.BOLD)

        //Can either do this
        //var radio = findViewById<RadioButton>(R.id.radioGroup)

        //Or use ViewBinding to get the radio group element. I prefer this way as it saves
        //the amount of times I need to enter findViewById
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == binding.radioButtonBlue.id) {
                binding.constraintLayout.setBackgroundColor(Color.parseColor("#65abd6"))
            }

            if(checkedId == binding.radioButtonGreen.id) {
                binding.constraintLayout.setBackgroundColor(Color.parseColor("#7fb880"))
            }

            if(checkedId == binding.radioButtonPink.id) {
                binding.constraintLayout.setBackgroundColor(Color.parseColor("#cca5cc"))
            }
        }
    }

    // This function is called when the user clicks the +/- button
    fun onPlusMinus(view: View) {
        // Check if there is no current error and if the last input was a number
        if (!stateError && lastNumeric) {
            // Get the current value displayed in the dataView and convert it to a double
            val currentValue = binding.dataView.text.toString().toDouble()
            // Check if the current value is not zero
            if (currentValue != 0.0) {
                // If the value is not zero, change its sign by multiplying it by -1
                val plusMinusDisplay = currentValue * -1.0
                // Update the dataView with the new value (with the sign changed)
                binding.dataView.text = plusMinusDisplay.toString()
            }
        }
    }



    fun onEqualClick(view: View) {
        onEqual()

        //sets the result of the outputView to the dataView
        binding.dataView.text = binding.outputView.text.toString().drop(1)

    }

    // Function called when any digit button (including the decimal point) is clicked
    fun onDigitClick(view: View) {
        // Get the text (digit or decimal point) from the button that was clicked
        val buttonText = (view as Button).text.toString()

        // Check if the calculator is in an error state (e.g., division by zero)
        if (stateError) {
            // If in an error state, replace the current text in the dataView with the clicked digit or decimal point
            binding.dataView.text = buttonText
            // Reset the error state
            stateError = false
        } else {
            // If the clicked button is the decimal point
            if (buttonText == ".") {
                // Check if the last input was numeric and no decimal point was input before
                if (lastNumeric && !lastDecimal) {
                    // If conditions met, append the decimal point to the dataView
                    binding.dataView.append(buttonText)
                    // Set lastDecimal to true, indicating that a decimal point has been entered
                    lastDecimal = true
                    // Set lastNumeric to false, as the last input is now a decimal point
                    lastNumeric = false
                }
            } else {
                // If the clicked button is a numeric digit, append it to the dataView
                binding.dataView.append(buttonText)
                // Set lastNumeric to true, as the last input is a numeric digit
                lastNumeric = true
            }
        }

        // Check if the clicked button is NOT a decimal point
        if (buttonText != ".") {
            // If it's a numeric digit, call the onEqual function to update the outputView with the result
            onEqual()
        }
    }


    fun onOperatorClick(view: View) {
        if(!stateError && lastNumeric) {
            //sets the plus/minus sign button off that way user cannot break program
            //after any operator is clicked
            binding.btnPlusMinus.isEnabled = false

            //will append operator view such as +, - etc to the dataView expression
            binding.dataView.append((view as Button).text)

            lastDecimal = false
            lastNumeric = false

            //calls onEqual to evaluate expression
            onEqual()
        }
    }

    // This function is called when the user clicks the backspace button
    fun onBackClick(view: View) {
        // Remove the last character from the expression in the dataView
        val newDataViewText = binding.dataView.text.toString().dropLast(1)
        // Update the dataView with the modified expression
        binding.dataView.text = newDataViewText

        // Get the new last character in the modified expression
        val lastChar = newDataViewText.lastOrNull()
        // Check what the last character is and update lastNumeric and lastDecimal accordingly
        when {
            // If there are no characters left in the expression, reset the state variables and clear the outputView
            lastChar == null -> {
                stateError = false
                lastDecimal = false
                lastNumeric = false
                binding.outputView.text = ""
            }
            // If the last character is a digit, set lastNumeric to true and lastDecimal to false
            lastChar.isDigit() -> {
                lastNumeric = true
                lastDecimal = false
            }
            // If the last character is a decimal point, set lastNumeric to false and lastDecimal to true
            lastChar == '.' -> {
                lastNumeric = false
                lastDecimal = true
            }
            // If the last character is an operator, set both lastNumeric and lastDecimal to false
            else -> {
                lastNumeric = false
                lastDecimal = false
            }
        }
        // Evaluate the current expression and update the outputView
        onEqual()
    }



    //Will clear all input views
    fun onClearClick(view: View) {
        binding.dataView.text = ""
        binding.outputView.text = ""
        binding.btnPlusMinus.isEnabled = true
        binding.btnEquals.isEnabled = true

        stateError = false
        lastDecimal = false
        lastNumeric = false
    }

    private fun onEqual() {
        if(lastNumeric && !stateError) {
            val text = binding.dataView.text.toString()

            var lastChar = binding.dataView.text.toString().last()

            expression = ExpressionBuilder(text).build()

            //catches error if there isn't something after the operator as well
            //this will help with the backspace button logic as well
            try {
                //need to check here as well for last character being a digit
                //since when user hits equal sign will evaluate
                if(lastChar.isDigit()) {
                    //Checks if the E letter is within an operation once number becomes too large
                    //If is the case disables the equals button to avoid an exception
                    if(text.contains("E")) {
                        binding.btnEquals.isEnabled = false
                    }

                    else {
                        binding.btnEquals.isEnabled = true

                        val result = expression.evaluate()

                        //As soon as we start evaluation answer box should display so we set the
                        //visibility to true
                        binding.outputView.visibility = View.VISIBLE

                        //Set the result of the answer display TextView
                        binding.outputView.text = "=" + result.toString()
                    }
                }
            }

            //This catch will also run if user tries to divide by 0
            catch (ex: Exception) {
                Log.e("Evaluate Error", ex.toString())
                binding.outputView.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }
}