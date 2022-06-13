package com.wizeline.academy.animations.ui.more_details

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.wizeline.academy.animations.databinding.MoreDetailsFragmentBinding
import com.wizeline.academy.animations.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MoreDetailsFragment : Fragment() {

    private var _binding: MoreDetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoreDetailsViewModel by viewModels()
    private val args: MoreDetailsFragmentArgs by navArgs()

    //scale animations
    lateinit var scaleXAnimation: SpringAnimation
    lateinit var scaleYAnimation: SpringAnimation
    lateinit var scaleGestureDetector: ScaleGestureDetector

    private companion object Params {
        const val STIFFNESS = SpringForce.STIFFNESS_HIGH
        const val DAMPING_RATIO_SMALL = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        const val INITIAL_SCALE = 1f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MoreDetailsFragmentBinding.inflate(inflater, container, false)
        binding.ivImageDetailLarge.loadImage(args.imageId)

        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

        postponeEnterTransition(100, TimeUnit.MILLISECONDS)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.title.observe(viewLifecycleOwner) { binding.tvTitle.text = it }
        viewModel.content.observe(viewLifecycleOwner) { binding.tvFullTextContent.text = it }
        viewModel.fetchData(args.contentIndex)

        binding.tvTitle.transitionName = "title_transition"
        binding.ivImageDetailLarge.transitionName = "image_transition"
        binding.tvFullTextContent.transitionName = "description_transition"

        manageXandYImageScale()
    }

    // this manages how big the scale gets affected
    private fun setupPinchToZoom() {
        var scaleFactor = 1f
        scaleGestureDetector = ScaleGestureDetector(requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    binding.ivImageDetailLarge.scaleX *= scaleFactor
                    binding.ivImageDetailLarge.scaleY *= scaleFactor
                    return true
                }
            })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun manageXandYImageScale() {
        // create scaleX and scaleY animations when returning
        scaleXAnimation = createSpringAnimation(
            binding.ivImageDetailLarge, SpringAnimation.SCALE_X,
            INITIAL_SCALE, STIFFNESS, DAMPING_RATIO_SMALL
        )
        scaleYAnimation = createSpringAnimation(
            binding.ivImageDetailLarge, SpringAnimation.SCALE_Y,
            INITIAL_SCALE, STIFFNESS, DAMPING_RATIO_SMALL
        )

        setupPinchToZoom()

        binding.ivImageDetailLarge.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // returns the image to the original scale and with custom animation
                scaleXAnimation.start()
                scaleYAnimation.start()
            } else {
                // cancel animations so we can grab the view during previous animation
                scaleXAnimation.cancel()
                scaleYAnimation.cancel()
                // pass touch event to ScaleGestureDetector and change the size
                scaleGestureDetector.onTouchEvent(event)
            }
            true
        }
    }

    private fun createSpringAnimation(
        view: View, property: DynamicAnimation.ViewProperty,
        finalPosition: Float,
        @FloatRange(from = 0.1) stiffness: Float,
        @FloatRange(from = 0.1) dampingRatio: Float
    ): SpringAnimation {
        val animation = SpringAnimation(view, property)
        val spring = SpringForce(finalPosition)
        spring.stiffness = stiffness
        spring.dampingRatio = dampingRatio
        animation.spring = spring
        return animation
    }
}